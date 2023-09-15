package io.prometheus.jmx;


import cn.iris.cloud.metrics.config.MetricsConfigProperties;

import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * MetricsConfig
 *
 * @author wuhao
 */
public class MetricsContextConfig {
    private boolean lowercaseOutputName;
    private boolean lowercaseOutputLabelNames;
    private List<ObjectName> whitelistObjectNames = new ArrayList<>();
    private List<ObjectName> blacklistObjectNames = new ArrayList<>();
    private List<JmxCollector.Rule> rules = new ArrayList<>();
    private MatchedRulesCache rulesCache;

    private Map<String, String> labelNamesValues;

    private MetricsConfigProperties metricsConfigProperties;

    public boolean isLowercaseOutputName() {
        return lowercaseOutputName;
    }

    public void setLowercaseOutputName(boolean lowercaseOutputName) {
        this.lowercaseOutputName = lowercaseOutputName;
    }

    public boolean isLowercaseOutputLabelNames() {
        return lowercaseOutputLabelNames;
    }

    public void setLowercaseOutputLabelNames(boolean lowercaseOutputLabelNames) {
        this.lowercaseOutputLabelNames = lowercaseOutputLabelNames;
    }

    public List<ObjectName> getWhitelistObjectNames() {
        return whitelistObjectNames;
    }

    public void setWhitelistObjectNames(List<ObjectName> whitelistObjectNames) {
        this.whitelistObjectNames = whitelistObjectNames;
    }

    public List<ObjectName> getBlacklistObjectNames() {
        return blacklistObjectNames;
    }

    public void setBlacklistObjectNames(List<ObjectName> blacklistObjectNames) {
        this.blacklistObjectNames = blacklistObjectNames;
    }

    public List<JmxCollector.Rule> getRules() {
        return rules;
    }

    public void setRules(List<JmxCollector.Rule> rules) {
        this.rules = rules;
    }

    public MatchedRulesCache getRulesCache() {
        return rulesCache;
    }

    public void setRulesCache(MatchedRulesCache rulesCache) {
        this.rulesCache = rulesCache;
    }


    public Map<String, String> getLabelNamesValues() {
        return labelNamesValues;
    }

    public void setLabelNamesValues(Map<String, String> labelNamesValues) {
        this.labelNamesValues = labelNamesValues;
    }

    public MetricsConfigProperties getMetricsConfigProperties() {
        return metricsConfigProperties;
    }

    public void setMetricsConfigProperties(MetricsConfigProperties metricsConfigProperties) {
        this.metricsConfigProperties = metricsConfigProperties;
    }
}