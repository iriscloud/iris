package cn.iris.cloud.chat.access.connect.plugin;

import cn.iris.cloud.chat.access.connect.manager.AccessMqttServerListener;
import cn.iris.cloud.protocols.mqtt.server.MqttServer;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * AccessConnectPlugin
 * @author wuhao
 * @description: AccessConnectPlugin
 * @createTime 2023/08/26 00:00:00
 */
@Service
public class AccessConnectPlugin {
	private static final Logger LOGGER = LoggerFactory.getLogger(AccessConnectPlugin.class);

	@Resource
	private AccessMqttServerListener accessMqttServerListener;
	@PostConstruct
	public MqttServer accessConnectMqttServer() {
		LOGGER.info("accessConnectMqttServer start.");
		MqttServer mqttServer = new MqttServer(1883, accessMqttServerListener);
		Thread thread = new Thread(() -> mqttServer.run(new NioEventLoopGroup(), new NioEventLoopGroup()));
		thread.start();
		LOGGER.info("accessConnectMqttServer complete.");
		return mqttServer;
	}
}
