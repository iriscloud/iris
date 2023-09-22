package cn.iris.cloud.protocols.mqtt.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;

public interface MqttServerListener {
	void create(ChannelHandlerContext ctx) throws Exception;

	void connect(MqttConnectMessage msg, ChannelHandlerContext ctx) throws Exception;

	void publish(MqttPublishMessage msg, ChannelHandlerContext ctx) throws Exception;

	void puback(MqttPubAckMessage msg, ChannelHandlerContext ctx) throws Exception;

	void pubrec(MqttMessage msg, ChannelHandlerContext ctx)
		throws Exception;

	void pubrel(MqttMessage msg, ChannelHandlerContext ctx)
		throws Exception;

	void pubcomp(MqttMessage msg, ChannelHandlerContext ctx)
		throws Exception;

	void subscribe(MqttSubscribeMessage msg, ChannelHandlerContext ctx)
		throws Exception;

	void unSubscribe(MqttUnsubscribeMessage msg, ChannelHandlerContext ctx)
		throws Exception;

	void pingReq(MqttMessage msg, ChannelHandlerContext ctx)
		throws Exception;


	void disconnect(MqttMessage msg, ChannelHandlerContext ctx) throws Exception;

	void closed(ChannelHandlerContext ctx) throws Exception;

	void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
		throws Exception;
}
