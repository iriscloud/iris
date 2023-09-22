package cn.iris.cloud.protocols.mqtt.server;

import cn.iris.cloud.protocols.mqtt.IServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttServer implements IServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(MqttServer.class);

	private final int port;
	private Channel channel = null;
	private MqttServerListener listener;

	public MqttServer(int port, MqttServerListener listener) {
		this.port = port;
		this.listener = listener;
	}


	@Override
	public void run(EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
		LOGGER.info("start mqtt server:{}", port);
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch)
					throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast("encoder", MqttEncoder.INSTANCE);
					pipeline.addLast("framer", new MqttDecoder());
//						pipeline.addLast(new ReadTimeoutHandler(300));
					pipeline.addLast("handler", new MqttMessageServerHandler(listener));
				}
			});
		try {
			channel = b.bind(port).sync().channel();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws Exception {
		channel.closeFuture().sync();
	}

}
