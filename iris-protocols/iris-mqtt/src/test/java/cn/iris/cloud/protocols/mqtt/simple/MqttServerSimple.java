package cn.iris.cloud.protocols.mqtt.simple;

import cn.iris.cloud.protocols.mqtt.server.MqttServer;
import cn.iris.cloud.protocols.mqtt.server.MqttServerListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.mqtt.*;

/**
 * @author wuhao
 * @description: MqttServerSimple
 * @createTime 2023/03/19 20:36:00
 */

public class MqttServerSimple {
	public static void main(String[] args) {
		MqttServer server = getServer();
		server.run(new NioEventLoopGroup(), new NioEventLoopGroup());

	}
	public static MqttServer getServer(){
		MqttServer server = new MqttServer(1883, new MqttServerListener() {
			@Override
			public void create(ChannelHandlerContext ctx) throws Exception {
				System.out.println("create");
			}

			@Override
			public void connect(MqttConnectMessage msg, ChannelHandlerContext ctx) throws Exception {
				System.out.println("connect"+ msg.variableHeader().name());
				MqttConnAckMessage connAckMessage = MqttMessageBuilders.connAck()
						.returnCode(MqttConnectReturnCode.CONNECTION_ACCEPTED).build();
				ctx.writeAndFlush(connAckMessage);
			}

			@Override
			public void publish(MqttPublishMessage msg, ChannelHandlerContext ctx) throws Exception {
				System.out.println("publish" + msg.variableHeader().topicName());
				MqttMessage mqttMessage = MqttMessageBuilders
						.pubAck().packetId(msg.variableHeader().packetId()).reasonCode((byte) 1)
						.build();
				ctx.writeAndFlush(mqttMessage);
			}

			@Override
			public void puback(MqttPubAckMessage msg, ChannelHandlerContext ctx) throws Exception {
				System.out.println("puback");
			}

			@Override
			public void pubrec(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
				System.out.println("pubrec");
			}

			@Override
			public void pubrel(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
				System.out.println("pubrel");
			}

			@Override
			public void pubcomp(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
				System.out.println("pubcomp");
			}

			@Override
			public void subscribe(MqttSubscribeMessage msg, ChannelHandlerContext ctx) throws Exception {
				System.out.println("subscribe" + msg.variableHeader().toString());
				MqttMessage mqttMessage = MqttMessageBuilders.subAck()
						.packetId(msg.variableHeader().messageId())
						.addGrantedQos(MqttQoS.AT_LEAST_ONCE).build();
				ctx.writeAndFlush(mqttMessage);

			}

			@Override
			public void unSubscribe(MqttUnsubscribeMessage msg, ChannelHandlerContext ctx) throws Exception {
				ctx.writeAndFlush(MqttMessageBuilders.unsubAck().build());
				System.out.println("unSubscribe");
			}

			@Override
			public void pingReq(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
				MqttFixedHeader pingreqFixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false,
						MqttQoS.AT_MOST_ONCE, false, 0);
				MqttMessage pingResp = new MqttMessage(pingreqFixedHeader);
				ctx.writeAndFlush(pingResp);
				System.out.println("pingReq");
			}

			@Override
			public void disconnect(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
				System.out.println("disconnect");
				ctx.close();
			}

			@Override
			public void closed(ChannelHandlerContext ctx) throws Exception {
				System.out.println("closed");
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
				System.out.println("exceptionCaught");
				cause.printStackTrace();
			}
		});
		return server;
	}
}
