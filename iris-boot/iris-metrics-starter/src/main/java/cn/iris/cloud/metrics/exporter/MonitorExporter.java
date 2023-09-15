package cn.iris.cloud.metrics.exporter;

import cn.iris.cloud.metrics.config.MetricsConfigProperties;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.jmx.MetricsJmxCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MonitorExporter
 *
 * @author wuhao
 */
public class MonitorExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorExporter.class);

    private static final String TAG = "jmx_scrape_duration_seconds";
    private MetricsJmxCollector jmxCollector;

    public MetricsJmxCollector getJmxCollector() {
        return jmxCollector;
    }

    public MonitorExporter(MetricsConfigProperties config) {
        try {
            jmxCollector = new MetricsJmxCollector(config);
            if (CollectorRegistry.defaultRegistry.getSampleValue(TAG) == null) {
                CollectorRegistry.defaultRegistry.register(jmxCollector);
            }
			HTTPServer httpServer = new HTTPServer(config.getIp(), config.getPort());
        } catch (Exception e) {
            LOGGER.error("MonitorExporter Error:", e);
        }
    }


}
