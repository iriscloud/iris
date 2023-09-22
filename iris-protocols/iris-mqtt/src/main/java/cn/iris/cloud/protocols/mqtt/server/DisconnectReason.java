package cn.iris.cloud.protocols.mqtt.server;

public enum DisconnectReason {

	/**
	 * 链接断开
	 */
	ConnectClose("1"),

	/**
	 * DisconnectMessage
	 */
	SDKDisconnectMessage("2"),

	/**
	 * SDK发送 DisconnectMessage logoff
	 */
	SDKDisconnectMessageLogoff("3"),

	/**
	 * SDK ping超时
	 */
	PingTimeoutDisconnect("4"),

	/**
	 * 服务端解析读包超时
	 */
	ReadDataTimeoutDisconnect("5"),

	/**
	 * 非法协议包 断链接
	 */
	BadMessage("6"),

	/**
	 * 其他异常断链接
	 */
	ExceptionDisconnect("7"),

	/**
	 * 其他设备登录
	 */
	OtherDeviceLogin("8"),

	/**
	 * 当非强制踢相同类型端时，关闭本端
	 */
	CloseSelf("9");

	private String value;

	private DisconnectReason(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
