package cn.iris.cloud.metrics.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CommonAutoConfiguration
 *
 * @author wuhao
 */
@Configuration
public class MetricsConfiguration {

	@Bean
	public MetricsConfigProperties metricsConfigProperties() {
		MetricsConfigProperties metricsConfigProperties = new MetricsConfigProperties();
		return metricsConfigProperties;
	}

	@Bean
	public MetricsAutoRegistration getMetricsAutoRegistration() {
		MetricsAutoRegistration registration = new MetricsAutoRegistration();
		registration.startRegAndExporter(metricsConfigProperties());
		return registration;
	}

}
