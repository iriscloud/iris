package cn.iris.cloud.metrics.jmx;

/**
 * Metrics 的 配套属性
 *
 * @author wuhao
 **/
public interface MetricsConfigMBean {

	/**
	 * needMetrics
	 *
	 * @return
	 */
	boolean needMetrics();

	/**
	 * setNeedMetrics
	 *
	 * @param isNeedMetrics
	 */
	void setNeedMetrics(boolean isNeedMetrics);

	/**
	 * needJvmMetrics
	 *
	 * @return
	 */
	boolean needJvmMetrics();

	/**
	 * setNeedJvmMetrics
	 *
	 * @param isNeedJvmMetrics
	 */
	void setNeedJvmMetrics(boolean isNeedJvmMetrics);

}
