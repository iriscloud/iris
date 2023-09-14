package cn.iris.cloud.metrics;

import cn.iris.cloud.common.extension.ExtensionLoader;
import cn.iris.cloud.common.utils.ConfigUtils;
import cn.iris.cloud.metrics.api.*;

/**
 * MetricsFactory
 *
 * @author wuhao
 */

public class MetricsFactory {
	public static MetricsBuilder BUILDER = ExtensionLoader.loadOrByDefaultFactory(MetricsBuilder.class,
			ConfigUtils.getProperty(MetricsBuilder.KEY, MetricsBuilder.DEFAULT), MetricsBuilderNop::new);

	public static MetricsTimer timer(String scope, String... names) {
		return BUILDER.timer(scope, names);
	}

	public static MetricsCounter counter(String scope, String... names) {
		return BUILDER.counter(scope, names);
	}

	public static MetricsMeter meter(String scope, String... names) {
		return BUILDER.meter(scope, names);
	}

	public static MetricsHistogram histogram(String scope, String... names) {
		return BUILDER.histogram(scope, names);
	}

	public static <T> void gauge(MetricsGauge<T> metricsGauge, String scope, String... names) {
		BUILDER.gauge(metricsGauge, scope, names);
	}
}
