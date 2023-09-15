package cn.iris.cloud.metrics.utils;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class Environments {

	private static final Map<String, String> VARS_MAP = new HashMap<>(32);
	private static final Map<String, String> VARS_CASE_INSENSITIVE = new HashMap<>(32);


	public static final String NODE_IP = "cn.iris.node.ip";
	public static final String NODE_NAME = "cn.iris.node.name";
	public static final String SERVER_APP = "cn.iris.server.app";

	static {
		loadSystemVariables();
	}

	private Environments() {
	}

	/**
	 * 加载系统配置
	 */
	public static void loadSystemVariables() {
		for (Map.Entry<String, String> e : System.getenv().entrySet()) {
			VARS_MAP.put(e.getKey(), e.getValue());
			VARS_CASE_INSENSITIVE.put(
				e.getKey().toLowerCase(), e.getValue());
		}
		Properties properties = System.getProperties();
		for (String pk : properties.stringPropertyNames()) {
			VARS_MAP.put(pk, properties.getProperty(pk));
			VARS_CASE_INSENSITIVE.put(
				pk.toLowerCase(), properties.getProperty(pk));
		}
	}

	// testing purpose
	public static void clearSystemVariables() {
		VARS_MAP.clear();
		VARS_CASE_INSENSITIVE.clear();
	}

	/**
	 * 得到一个环境变量
	 */
	public static String getVar(String key) {
		return VARS_MAP.get(key);
	}

	/**
	 * 建议区分大小写 {@link #getVar(String)}
	 */
	public static String getVarCaseInsensitive(String key) {
		return VARS_CASE_INSENSITIVE.get(key.toLowerCase());
	}

	public static String getNodeName() {
		return getVarCaseInsensitive(NODE_NAME);
	}

	public static String getNodeIp() {
		return getVarCaseInsensitive(NODE_IP);
	}

	public static String getServerApp() {
		return getVarCaseInsensitive(SERVER_APP);
	}

	public static void resetSystemProperty(String key, String value) {
		VARS_MAP.put(key, value);
		VARS_CASE_INSENSITIVE.put(key.toLowerCase(), value);
	}
}
