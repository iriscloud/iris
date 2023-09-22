package cn.iris.cloud.chat.access.connect.log;

import cn.iris.cloud.metrics.MetricsFactory;
import cn.iris.cloud.metrics.api.MetricsMeter;

/**
 * @author wuhao
 * @description: MetricsUtils
 * @createTime 2023/08/31 23:58:00
 */

public class MetricsUtils {
	private static final String UP = "up";
	private static final String DOWN = "down";
	public static MetricsMeter getUpMetrics(){
		return MetricsFactory.meter("service", UP);
	}
	public static MetricsMeter getDownMetrics(){
		return MetricsFactory.meter("service", DOWN);
	}
}
