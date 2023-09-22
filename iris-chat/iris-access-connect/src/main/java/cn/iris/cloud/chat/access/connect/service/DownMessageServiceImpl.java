package cn.iris.cloud.chat.access.connect.service;


import cn.iris.cloud.chat.access.connect.manager.UserCtxManager;
import cn.iris.cloud.chat.message.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
/**
 * DownMessageServiceImpl
 * @author wuhao
 * @description: DownMessageServiceImpl
 * @createTime 2023/08/26 00:00:00
 */
@Service(interfaceClass = DownMessageService.class)
@Component
public class DownMessageServiceImpl implements DownMessageService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DownMessageServiceImpl.class);

	@Override
	public DownMessageResponse processMessage(DownMessageRequest request) {
		LOGGER.info("DownMessage:{}:{}", request.getTid(), request.getFrom());

		ByteBuf content = Unpooled.wrappedBuffer(request.getContent().getBytes());
		MqttPublishMessage msg = MqttMessageBuilders.publish()
			.topicName(request.getTopic())
			.qos(MqttQoS.AT_MOST_ONCE)
			.payload(content)
			.build();
		ChannelHandlerContext ctx = UserCtxManager.getUser(request.getTo(), null);
		ctx.writeAndFlush(msg);

		DownMessageResponse response = new DownMessageResponse();
		return response;
	}
}