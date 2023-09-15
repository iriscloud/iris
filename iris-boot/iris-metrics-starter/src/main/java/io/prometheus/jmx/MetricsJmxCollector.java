package io.prometheus.jmx;

import cn.iris.cloud.metrics.config.MetricsConfigProperties;
import cn.iris.cloud.metrics.utils.MetricsConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * MetricsJmxCollector
 *
 * @author wuhao
 */
public class MetricsJmxCollector extends JmxCollector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsJmxCollector.class);
    private static final Pattern DEFAULT_PATTERN = Pattern.compile(".*");
    private static final JmxMBeanPropertyCache jmxMBeanPropertyCache = new JmxMBeanPropertyCache();

    private static String defaultConfig = "rules:\n" +
            "  - pattern: '.*'";
    private Map<String, String> labelNamesValues;

    private MetricsConfigProperties metricsConfigProperties;
    
    public MetricsJmxCollector(MetricsConfigProperties metricsConfigProperties) throws MalformedObjectNameException {
        super(defaultConfig);
        this.labelNamesValues = new ConcurrentHashMap<>();
        this.metricsConfigProperties = metricsConfigProperties;
    }


    @Override
    public List<MetricFamilySamples> collect() {
        MetricsContextConfig config = getJmxConfig();
        MatchedRulesCache.StalenessTracker stalenessTracker = new MatchedRulesCache.StalenessTracker();
        MetricsReceiver receiver = new MetricsReceiver(config, stalenessTracker);
        JmxScraper scraper = new JmxScraper("", "", "", false, config.getWhitelistObjectNames(),
                config.getBlacklistObjectNames(), receiver, jmxMBeanPropertyCache);
        long start = System.nanoTime();
        double error = 0;
        try {
            scraper.doScrape();
        } catch (Exception e) {
            error = 1;
            LOGGER.warn("collect error:", e);
        }
        config.getRulesCache().evictStaleEntries(stalenessTracker);

        List<MetricFamilySamples> mfsList = new ArrayList<>();
        mfsList.addAll(receiver.metricFamilySamplesMap.values());
        List<MetricFamilySamples.Sample> samples = new ArrayList<>();
        samples.add(new MetricFamilySamples.Sample(
                "jmx_scrape_duration_seconds", new ArrayList<>(), new ArrayList<>(), (System.nanoTime() - start) / 1.0E9));
        mfsList.add(new MetricFamilySamples("jmx_scrape_duration_seconds", Type.GAUGE, "Time this JMX scrape took, in seconds.", samples));

        samples = new ArrayList<>();
        samples.add(new MetricFamilySamples.Sample(
                "jmx_scrape_error", new ArrayList<>(), new ArrayList<>(), error));
        mfsList.add(new MetricFamilySamples("jmx_scrape_error", Type.GAUGE, "Non-zero if this scrape failed.", samples));
        samples = new ArrayList<>();
        samples.add(new MetricFamilySamples.Sample(
                "jmx_scrape_cached_beans", new ArrayList<>(), new ArrayList<>(), stalenessTracker.cachedCount()));
        mfsList.add(new MetricFamilySamples("jmx_scrape_cached_beans", Type.GAUGE, "Number of beans with their matching rule cached", samples));
        return mfsList;
    }


    public void setTag(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return;
        }
        labelNamesValues.put(key, value);
    }

    private MetricsContextConfig getJmxConfig() {
        MetricsContextConfig metricsConfig = new MetricsContextConfig();
        try {
            metricsConfig.setLabelNamesValues(labelNamesValues);
            metricsConfig.setWhitelistObjectNames(Arrays.asList(new ObjectName(MetricsConstant.METRICS_JMX)));
            metricsConfig.setMetricsConfigProperties(metricsConfigProperties);
            Rule rule = new Rule();
            rule.pattern = DEFAULT_PATTERN;
            metricsConfig.getRules().add(rule);
            metricsConfig.setRulesCache(new MatchedRulesCache(metricsConfig.getRules()));
        } catch (MalformedObjectNameException e) {
            LOGGER.warn("getJmxConfig Error: ", e);
        }
        return metricsConfig;
    }
}

