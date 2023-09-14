package cn.iris.cloud.metrics.api;

/**
 * MetricsContext
 *
 * @author wuhao
 */

public interface MetricsContext extends AutoCloseable {
	/**
	 * timer
	 *
	 * @return
	 */
	long stop();
}
