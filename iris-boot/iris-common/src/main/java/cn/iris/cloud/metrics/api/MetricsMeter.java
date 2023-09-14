package cn.iris.cloud.metrics.api;

/**
 * MetricsMeter
 *
 * @author wuhao
 */

public interface MetricsMeter {

	/**
	 * mark
	 *
	 * @param n
	 */
	void mark(long n);

	/**
	 * mark
	 */
	default void mark() {
		mark(1);
	}

}
