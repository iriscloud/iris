package cn.iris.cloud.protocols.mqtt.server;

import io.netty.util.AttributeKey;

/**
 * MqttAttrKey
 */
public class MqttAttrKey {


	public static final AttributeKey<ConnState> CONN_STATE = AttributeKey.valueOf("STATE.connState");

	public static final AttributeKey<ProtocolType> PROTOCOL_TYPE = AttributeKey.valueOf("STATE.protocolType");

	public static final AttributeKey<String> DISCONN_REASON = AttributeKey.valueOf("STATE.disConnReason");

	public enum ConnState {
		/**
		 * 激活
		 */
		ACTIVE,

		/**
		 * 登录
		 */
		LOGIN,

		/**
		 * 关闭
		 */
		OFFLINE;
	}

	public enum ProtocolType {
		MQTT,

		WS;
	}


}
