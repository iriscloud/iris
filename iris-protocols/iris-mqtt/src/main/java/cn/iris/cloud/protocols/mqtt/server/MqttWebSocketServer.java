package cn.iris.cloud.protocols.mqtt.server;

import cn.iris.cloud.protocols.mqtt.IServer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import java.util.List;

/**
 * MqttWebSocketServer
 */
public class MqttWebSocketServer implements IServer {

	private final int port;
	private MqttServerListener listener;
	private Channel channel = null;

	private static final String MQTT_SUBPROTOCOL_CSV_LIST = "mqtt, mqttv3.1, mqttv3.1.1";
	private static final String WEB_SOCKET_PATH = "/mqtt";

	public MqttWebSocketServer(int port, MqttServerListener listener) {
		this.port = port;
		this.listener = listener;
	}

	public void run(EventLoopGroup bossGroup, EventLoopGroup workerGroup)
		throws Exception {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					pipeline.addLast("codec-http", new HttpServerCodec());
					pipeline.addLast("aggregator", new HttpObjectAggregator(65536 * 4));
					pipeline.addLast("webSocketHandler",
						new WebSocketServerProtocolHandler(WEB_SOCKET_PATH, MQTT_SUBPROTOCOL_CSV_LIST,
							false, 65536));
					pipeline.addLast("ws2bytebufDecoder", new WebSocketFrameToByteBufDecoder());
					pipeline.addLast("bytebuf2wsEncoder", new ByteBufToWebSocketFrameEncoder());
					pipeline.addLast(new ReadTimeoutHandler(300));
				}
			});

		channel = b.bind(port).sync().channel();
	}

	@Override
	public void close() throws Exception {
		channel.closeFuture().sync();
	}

	static class WebSocketFrameToByteBufDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {

		@Override
		protected void decode(ChannelHandlerContext chc, BinaryWebSocketFrame frame, List<Object> out)
			throws Exception {
			ByteBuf bb = frame.content();
			bb.retain();
			out.add(bb);
		}
	}

	static class ByteBufToWebSocketFrameEncoder extends MessageToMessageEncoder<ByteBuf> {

		@Override
		protected void encode(ChannelHandlerContext chc, ByteBuf bb, List<Object> out) throws Exception {
			// convert the ByteBuf to a WebSocketFrame
			BinaryWebSocketFrame result = new BinaryWebSocketFrame();
			result.content().writeBytes(bb);
			out.add(result);
		}
	}

}
