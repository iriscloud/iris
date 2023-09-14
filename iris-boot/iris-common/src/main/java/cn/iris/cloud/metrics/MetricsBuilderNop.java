package cn.iris.cloud.metrics;

import cn.iris.cloud.metrics.api.*;

import java.util.concurrent.Callable;
import java.util.function.LongConsumer;

/**
 * MetricsBuilderNop
 *
 * @author wuhao
 */

public class MetricsBuilderNop implements MetricsBuilder {
	@Override
	public MetricsTimer timer(String scope, String... names) {
		MetricsContext metricsContext =  new MetricsContext() {
			@Override
			public long stop() {
				return 0;
			}

			@Override
			public void close() throws Exception {

			}
		};
		return new MetricsTimer() {
			@Override
			public MetricsContext time() {
				return metricsContext;
			}

			@Override
			public MetricsContext time(LongConsumer elapsed) {
				return metricsContext;
			}

			@Override
			public void time(Runnable event) {

			}

			@Override
			public void time(Runnable event, LongConsumer elapsed) {

			}

			@Override
			public <T> T time(Callable<T> event) throws Exception {
				return event.call();
			}

			@Override
			public <T> T time(Callable<T> event, LongConsumer elapsed) throws Exception {
				return event.call();
			}
		};
	}

	@Override
	public MetricsCounter counter(String scope, String... names) {
		return new MetricsCounter() {
			@Override
			public void inc(long n) {

			}

			@Override
			public void dec(long n) {

			}
		};
	}

	@Override
	public MetricsMeter meter(String scope, String... names) {
		return n -> {
		};
	}

	@Override
	public MetricsHistogram histogram(String scope, String... names) {
		return new MetricsHistogram() {
			@Override
			public void update(int value) {

			}

			@Override
			public void update(long value) {

			}

			@Override
			public long getCount() {
				return 0;
			}
		};
	}

	@Override
	public <T> void gauge(MetricsGauge<T> metric, String scope, String... names) {

	}
}
