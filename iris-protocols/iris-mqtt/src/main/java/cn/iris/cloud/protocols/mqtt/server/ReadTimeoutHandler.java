package cn.iris.cloud.protocols.mqtt.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * ReadTimeoutHandler
 */
public class ReadTimeoutHandler extends ChannelInboundHandlerAdapter {

	private final long timeoutMillis;

	private volatile ScheduledFuture<?> timeout;
	private volatile long lastReadTime;

	private volatile int state; // 0 - none, 1 - Initialized, 2 - Destroyed;

	private boolean closed;
	private final static Logger logger = LoggerFactory
		.getLogger(ReadTimeoutHandler.class);

	/**
	 * Creates a new instance.
	 *
	 * @param timeoutSeconds read timeout in seconds
	 */
	public ReadTimeoutHandler(int timeoutSeconds) {
		this(timeoutSeconds, TimeUnit.SECONDS);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param timeout read timeout
	 * @param unit    the {@link TimeUnit} of {@code timeout}
	 */
	public ReadTimeoutHandler(long timeout, TimeUnit unit) {
		if (unit == null) {
			throw new NullPointerException("unit");
		}

		if (timeout <= 0) {
			timeoutMillis = 0;
		} else {
			timeoutMillis = Math.max(unit.toMillis(timeout), 1);
		}
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
			// channelActvie() event has been fired already, which means
			// this.channelActive() will
			// not be invoked. We have to initialize here instead.
			initialize(ctx);
		} else {
			// channelActive() event has not been fired yet.
			// this.channelActive() will be invoked
			// and initialization will occur there.
		}
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		destroy();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		// Initialize early if channel is active already.
		if (ctx.channel().isActive()) {
			initialize(ctx);
		}
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		initialize(ctx);
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		destroy();
		super.channelInactive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
		throws Exception {
		lastReadTime = System.currentTimeMillis();
		ctx.fireChannelRead(msg);
	}

	private void initialize(ChannelHandlerContext ctx) {
		// Avoid the case where destroy() is called before scheduling timeouts.
		// See: https://github.com/netty/netty/issues/143
		switch (state) {
			case 1:
			case 2:
				return;
		}

		state = 1;

		lastReadTime = System.currentTimeMillis();
		if (timeoutMillis > 0) {
			timeout = ctx.executor().schedule(new ReadTimeoutTask(ctx),
				timeoutMillis, TimeUnit.MILLISECONDS);
		}
	}

	private void destroy() {
		state = 2;

		if (timeout != null) {
			timeout.cancel(false);
			timeout = null;
		}
	}

	/**
	 * Is called when a read timeout was detected.
	 */
	protected void readTimedOut(ChannelHandlerContext ctx) throws Exception {
		if (!closed) {
			ctx.channel().attr(MqttAttrKey.DISCONN_REASON).set(DisconnectReason.PingTimeoutDisconnect.getValue());
			logger.info("ReadTimeoutException remote:{}\t{}", ctx.channel().remoteAddress(), ctx.channel().attr(MqttAttrKey.DISCONN_REASON).get());
			ctx.close();
			closed = true;
		}
	}

	private final class ReadTimeoutTask implements Runnable {

		private final ChannelHandlerContext ctx;

		ReadTimeoutTask(ChannelHandlerContext ctx) {
			this.ctx = ctx;
		}

		@Override
		public void run() {
			if (!ctx.channel().isOpen()) {
				return;
			}

			long currentTime = System.currentTimeMillis();
			long nextDelay = timeoutMillis - (currentTime - lastReadTime);
			if (nextDelay <= 0) {
				// Read timed out - set a new timeout and notify the callback.
				timeout = ctx.executor().schedule(this, timeoutMillis,
					TimeUnit.MILLISECONDS);
				try {
					readTimedOut(ctx);
				} catch (Throwable t) {
					ctx.fireExceptionCaught(t);
				}
			} else {
				// Read occurred before the timeout - set a new timeout with
				// shorter delay.
				timeout = ctx.executor().schedule(this, nextDelay,
					TimeUnit.MILLISECONDS);
			}
		}
	}
}
