package cn.iris.cloud.metrics.config;

import cn.iris.cloud.metrics.exporter.MonitorExporter;
import cn.iris.cloud.metrics.register.ServerRegisterTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MetricsAutoConfiguration
 *
 * @author wuhao
 */
public class MetricsAutoRegistration {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetricsAutoRegistration.class);

	/**
	 * startRegAndExporter
	 *
	 * @return
	 */
	public boolean startRegAndExporter(MetricsConfigProperties properties) {
		startExporter(properties);
		startReg(properties);
		return true;
	}

	/**
	 * startExporter
	 */
	public void startExporter(MetricsConfigProperties properties) {
		MonitorExporter monitorExporter = new MonitorExporter(properties);
		for (MetricsConfigTag type : MetricsConfigTag.getTags(properties)) {
			monitorExporter.getJmxCollector().setTag(type.getItem(), type.getValue());
		}

	}

	/**
	 * startReg
	 *
	 * @param properties
	 */
	public void startReg(MetricsConfigProperties properties) {
		ServerRegisterTask serverRegisterTask = new ServerRegisterTask();
		serverRegisterTask.start(properties);
	}


}
