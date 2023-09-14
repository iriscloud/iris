package cn.iris.cloud.metrics.api;

/**
 * MetricsGauge
 *
 * @author wuhao
 */

public interface MetricsGauge<T> {
	/**
	 * getValue
	 *
	 * @return
	 */
	T getValue();
}
