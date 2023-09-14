package cn.iris.cloud.metrics;

import cn.iris.cloud.metrics.api.*;
import org.junit.jupiter.api.Test;

/**
 * MetricsFactoryTest
 *
 * @author wuhao
 */

public class MetricsBuilderSimple implements MetricsBuilder{
	@Test
	void testMetrics() {

	}

	@Override
	public MetricsTimer timer(String scope, String... names) {
		return null;
	}

	@Override
	public MetricsCounter counter(String scope, String... names) {
		return null;
	}

	@Override
	public MetricsMeter meter(String scope, String... names) {
		return null;
	}

	@Override
	public MetricsHistogram histogram(String scope, String... names) {
		return null;
	}

	@Override
	public <T> void gauge(MetricsGauge<T> metric, String scope, String... names) {

	}


}
