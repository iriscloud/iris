package cn.iris.cloud.metrics.builder;


import cn.iris.cloud.metrics.MetricsBuilder;
import cn.iris.cloud.metrics.api.*;
import cn.iris.cloud.metrics.jmx.MetricRegistryManager;
import cn.iris.cloud.metrics.utils.MetricsConstant;
import com.codahale.metrics.*;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.LongConsumer;

/**
 * MetricsBuilderImpl
 *
 * @author wuhao
 */

public class MetricsBuilderImpl implements MetricsBuilder {

	@Override
	public MetricsTimer timer(String scope, String... names) {
		return new InnerTimer(registry(), scope, names);
	}

	@Override
	public MetricsCounter counter(String scope, String... names) {
		return new InnerCounter(registry(), scope, names);
	}

	@Override
	public MetricsMeter meter(String scope, String... names) {
		return new InnerMeter(registry(), scope, names);
	}

	@Override
	public MetricsHistogram histogram(String scope, String... names) {
		return new InnerHistogram(registry(), scope, names);
	}

	@Override
	public <T> void gauge(MetricsGauge<T> gauge, String scope, String... names) {
		registry().register(name(MetricsConstant.GAUGE, scope, names), new Gauge<T>() {
			@Override
			public T getValue() {
				return gauge.getValue();
			}
		});
	}

	private static String name(String metric, String scope, String[] names) {
		String prefix = scope + MetricsConstant.SEP + metric;
		if (names == null || names.length < 1) {
			return prefix;
		}
		return MetricRegistry.name(prefix, names);
	}

	private static MetricRegistry registry() {
		return MetricRegistryManager.getInstance().getMetricRegistry();
	}

	public static class InnerCounter implements MetricsCounter {

		private final Counter _counter;

		public InnerCounter(MetricRegistry registry, String scope, String... names) {
			this._counter = registry.counter(name(MetricsConstant.COUNTER, scope, names));

		}

		@Override
		public void inc(long n) {
			this._counter.inc(n);
		}

		@Override
		public void dec(long n) {
			this._counter.dec(n);
		}
	}

	public static class InnerMeter implements MetricsMeter {

		private final Meter _meter;

		public InnerMeter(MetricRegistry registry, String scope, String... names) {
			this._meter = registry.meter(name(MetricsConstant.METER, scope, names));

		}

		@Override
		public void mark(long n) {
			this._meter.mark(n);
		}

		@Override
		public void mark() {
			this._meter.mark();
		}
	}

	public static class InnerTimer implements MetricsTimer {
		private final Timer _timer;
		private final Meter _meter;
		private final Counter _counter;

		InnerTimer(MetricRegistry registry, String scope, String... names) {
			this._timer = registry.timer(name(MetricsConstant.TIMER, scope, names));
			this._meter = registry.meter(name(MetricsConstant.METER, scope, names));
			this._counter = registry.counter(name(MetricsConstant.COUNTER, scope, names));
		}

		/**
		 * 手工设置耗时
		 *
		 * @param duration
		 * @param unit
		 */
		public void setCostTime(long duration, TimeUnit unit) {
			this._meter.mark();
			this._counter.inc();
			this._timer.update(duration, unit);
		}

		public Context time() {
			return this.time((LongConsumer) null);
		}

		public Context time(LongConsumer elapsed) {
			this._meter.mark();
			this._counter.inc();
			return new Context(this._timer.time(), this._counter, elapsed);
		}

		public void time(Runnable event) {
			this.time(event, null);
		}

		public void time(Runnable event, LongConsumer elapsed) {
			try (Context ctx = this.time(elapsed)) {
				event.run();
			}
		}

		public <T> T time(Callable<T> event) throws Exception {
			return time(event, null);
		}

		public <T> T time(Callable<T> event, LongConsumer elapsed) throws Exception {
			try (Context ctx = this.time(elapsed)) {
				return event.call();
			}
		}

		public static class Context implements MetricsContext {
			private final com.codahale.metrics.Timer.Context _context;
			private final Counter _counter;
			private LongConsumer _elapsed;

			Context(com.codahale.metrics.Timer.Context context, Counter counter, LongConsumer consume) {
				this._context = context;
				this._counter = counter;
				this._elapsed = consume;
			}

			@Override
			public long stop() {
				this._counter.dec();
				long elapsed = this._context.stop();

				LongConsumer consume = this._elapsed;
				if (consume != null) {
					consume.accept(elapsed);
				}
				return elapsed;
			}

			@Override
			public void close() {
				this.stop();
			}
		}
	}

	public static class InnerHistogram implements MetricsHistogram {
		private final Histogram _histogram;

		public InnerHistogram(MetricRegistry registry, String scope, String... names) {
			this._histogram = registry.histogram(name(MetricsConstant.HISTOGRAM, scope, names));
		}

		@Override
		public void update(int value) {
			this._histogram.update(value);
		}

		@Override
		public void update(long value) {
			this._histogram.update(value);
		}

		@Override
		public long getCount() {
			return this._histogram.getCount();
		}
	}

}
