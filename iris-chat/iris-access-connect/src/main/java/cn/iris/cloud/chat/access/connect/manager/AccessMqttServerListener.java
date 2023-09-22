package cn.iris.cloud.chat.access.connect.manager;

import cn.iris.cloud.chat.access.connect.utils.ConnectAttrKey;
import cn.iris.cloud.chat.message.UpMessageRequest;
import cn.iris.cloud.chat.message.UpMessageResponse;
import cn.iris.cloud.chat.message.UpMessageService;
import cn.iris.cloud.protocols.mqtt.server.MqttServerListener;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * AccessMqttServerListener
 * @author wuhao
 * @description: AccessMqttServerListener
 * @createTime 2023/08/26 00:00:00
 */
@Service
public class AccessMqttServerListener implements MqttServerListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccessMqttServerListener.class);

	@DubboReference(protocol = "dubbo", check = false)
	private UpMessageService routeService;

	@Override
	public void create(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("create from addr:{}", ctx.channel().remoteAddress().toString());
	}

	@Override
	public void connect(MqttConnectMessage msg, ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("connect from addr:{}", ctx.channel().remoteAddress().toString());

		String clientId = msg.payload().clientIdentifier();

		UserCtxManager.putUser(clientId, null, ctx);
		ctx.channel().attr(ConnectAttrKey.CLIENT_ID).set(clientId);

		MqttConnAckMessage connAckMessage = MqttMessageBuilders.connAck()
			.returnCode(MqttConnectReturnCode.CONNECTION_ACCEPTED).build();
		ctx.writeAndFlush(connAckMessage);

	}

	@Override
	public void publish(MqttPublishMessage msg, ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("publish from addr:{}", ctx.channel().remoteAddress().toString());

		UpMessageRequest messageRequest = new UpMessageRequest();
		messageRequest.setFrom(ctx.channel().attr(ConnectAttrKey.CLIENT_ID).get());
		messageRequest.setContent(ByteBufUtil.hexDump(msg.content()));
		messageRequest.setTopic(msg.variableHeader().topicName());
		routeService.processMessage(messageRequest);

	}

	@Override
	public void puback(MqttPubAckMessage msg, ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("puback from addr:{}", ctx.channel().remoteAddress().toString());
	}

	@Override
	public void pubrec(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("pubrec from addr:{}", ctx.channel().remoteAddress().toString());
	}

	@Override
	public void pubrel(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("pubrel from addr:{}", ctx.channel().remoteAddress().toString());
	}

	@Override
	public void pubcomp(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("pubcomp from addr:{}", ctx.channel().remoteAddress().toString());
	}

	@Override
	public void subscribe(MqttSubscribeMessage msg, ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("subscribe:{} from addr:{}", 
				msg.variableHeader().toString()
				,ctx.channel().remoteAddress().toString());
		MqttMessage mqttMessage = MqttMessageBuilders.subAck()
				.packetId(msg.variableHeader().messageId())
				.addGrantedQos(MqttQoS.AT_LEAST_ONCE).build();
		ctx.writeAndFlush(mqttMessage);

	}

	@Override
	public void unSubscribe(MqttUnsubscribeMessage msg, ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("unSubscribe:{} from addr:{}",
				msg.variableHeader().toString()
				,ctx.channel().remoteAddress().toString());
		MqttMessage mqttMessage = MqttMessageBuilders.unsubAck()
				.packetId(msg.variableHeader().messageId()).build();
		ctx.writeAndFlush(mqttMessage);
	}

	@Override
	public void pingReq(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("pingReq from addr:{}",ctx.channel().remoteAddress().toString());
		MqttFixedHeader pingreqFixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false,
				MqttQoS.AT_MOST_ONCE, false, 0);
		MqttMessage pingResp = new MqttMessage(pingreqFixedHeader);
		ctx.writeAndFlush(pingResp);
	}

	@Override
	public void disconnect(MqttMessage msg, ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("disconnect from addr:{}"
			,ctx.channel().remoteAddress().toString());
		ctx.close();
	}

	@Override
	public void closed(ChannelHandlerContext ctx) throws Exception {
		LOGGER.info("closed from addr:{}", ctx.channel().remoteAddress().toString());
	}

	@Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.info("exceptionCaught from addr:{}", ctx.channel().remoteAddress().toString());
	}
}
