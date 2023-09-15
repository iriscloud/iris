package cn.iris.cloud.metrics.utils;

/**
 * MetricsConstant
 *
 * @author wuhao
 */
public class MetricsConstant {
	private MetricsConstant() {
	}


	public static final String SEP = "_";

	public static final String METRICS = "metrics";
	public static final String METRICS_JMX = "metrics:*";
	public static final String METRICS_CONFIG = "metrics:name=MetricsConfig";
	public static final String METRICS_SERVICE = "metrics:name=MetricsServiceInfo";
	public static final String DOMAIN_SOURCE = "metrics";
	public static final String DOMAIN_TARGET = "apm";


	public static final String L = "L";
	public static final String TYPE = "Type";
	public static final String METHOD = "Method";
	public static final String ID = "ID";

	public static final String METRICS_PATH = "/metrics";
	public static final String HEALTH_PATH = "/health";
	public static final String METRICS_URL = "http://%s:%s" + METRICS_PATH;
	public static final String HEALTH_URL = "http://%s:%s" + HEALTH_PATH;

	public static final String NAME = "name";


	///type
	public static final String GAUGE = "gauge";
	public static final String TIMER = "timer";
	public static final String COUNTER = "counter";
	public static final String METER = "meter";
	public static final String HISTOGRAM = "histogram";

}
