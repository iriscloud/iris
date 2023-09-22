package cn.iris.cloud.protocols.mqtt.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MqttMessageServerHandler
 *
 * @author wuhao
 */
public class MqttMessageServerHandler extends SimpleChannelInboundHandler<MqttMessage> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MqttMessageServerHandler.class);

	private final MqttServerListener listener;

	public MqttMessageServerHandler(MqttServerListener listener) {
		this.listener = listener;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		ctx.channel().attr(MqttAttrKey.CONN_STATE).set(MqttAttrKey.ConnState.ACTIVE);
		ctx.channel().attr(MqttAttrKey.PROTOCOL_TYPE).set(MqttAttrKey.ProtocolType.MQTT);
		if (listener != null) {
			listener.create(ctx);
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		ctx.channel().attr(MqttAttrKey.CONN_STATE).set(MqttAttrKey.ConnState.OFFLINE);
		if (listener != null) {
			listener.closed(ctx);
		}
		ctx.channel().close();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, MqttMessage msg) {
		try {
			if (msg == null) {
				return;
			}
			LOGGER.debug("process:{}", msg.fixedHeader().messageType());
			switch (msg.fixedHeader().messageType()) {
				case CONNECT:
					listener.connect((MqttConnectMessage) msg, ctx);
					break;
				case PUBLISH:
					listener.publish((MqttPublishMessage) msg, ctx);
					break;
				case PUBACK:
					listener.puback((MqttPubAckMessage) msg, ctx);
					break;
				case PUBREC:
					listener.pubrec(msg, ctx);
					break;
				case PUBREL:
					listener.pubrel(msg, ctx);
					break;
				case PUBCOMP:
					listener.pubcomp(msg, ctx);
					break;
				case SUBSCRIBE:
					listener.subscribe((MqttSubscribeMessage) msg, ctx);
					break;
				case UNSUBSCRIBE:
					listener.unSubscribe((MqttUnsubscribeMessage) msg, ctx);
					break;
				case PINGREQ:
					listener.pingReq(msg, ctx);
					break;
				case DISCONNECT:
					listener.disconnect(msg, ctx);
					break;
				default:
					break;
			}
		} catch (Exception e) {
			LOGGER.warn("channelRead0 Error:", e);
		}

	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
		throws Exception {

		ctx.channel().attr(MqttAttrKey.DISCONN_REASON).set(DisconnectReason.ExceptionDisconnect.getValue());

		if (listener != null) {
			listener.exceptionCaught(ctx, cause);
		}

		ctx.channel().close();
	}

}
