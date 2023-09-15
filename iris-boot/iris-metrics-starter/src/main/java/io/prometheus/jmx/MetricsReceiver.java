package io.prometheus.jmx;

import cn.iris.cloud.metrics.config.MetricsConfigProperties;
import cn.iris.cloud.metrics.utils.MetricsConstant;
import io.prometheus.client.Collector;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;

import static io.prometheus.jmx.JmxCollector.safeName;
import static io.prometheus.jmx.JmxCollector.toSnakeAndLowerCase;

public class MetricsReceiver implements JmxScraper.MBeanReceiver {
    Map<String, Collector.MetricFamilySamples> metricFamilySamplesMap = new HashMap<>();
    Set<MetricsSampleKey> sampleKeys = new HashSet<>();
    MetricsContextConfig config;
    MatchedRulesCache.StalenessTracker stalenessTracker;


    public MetricsReceiver(MetricsContextConfig config, MatchedRulesCache.StalenessTracker stalenessTracker) {
        this.config = config;
        this.stalenessTracker = stalenessTracker;
    }

    public void recordBean(
            String domain,
            LinkedHashMap<String, String> beanProperties,
            LinkedList<String> attrKeys,
            String attrName,
            String attrType,
            String attrDescription,
            Object beanValue) {

        domain = replaceDomain(domain);
        String beanName = domain + angleBrackets(beanProperties.toString()) + angleBrackets(attrKeys.toString());
        String attrNameSnakeCase = toSnakeAndLowerCase(attrName);
        MatchedRule matchedRule = MatchedRule.unmatched();

        for (JmxCollector.Rule rule : config.getRules()) {
            String matchName = getMatchName(rule, beanValue, beanName, attrNameSnakeCase, attrName);
            MatchedRule cachedRule = getMatchedRuleCache(rule, matchName);
            if (cachedRule != null) {
                break;
            }
            if (rule.cache) {
                continue;
            }
            Matcher matcher = rule.pattern.matcher(matchName);
            if (!matcher.matches()) {
                addToCache(rule, matchName, MatchedRule.unmatched());
                continue;
            }
            Double value = getDoubleValue(rule, matcher);

            String tagName = rule.attrNameSnakeCase ? attrNameSnakeCase : attrName;
            if (checkIgnoreTag(tagName)) {
                return;
            }
            if (StringUtils.isBlank(rule.name)) {
                matchedRule = defaultExport(matchName, domain, beanProperties, tagName, value, rule.valueFactor);

                addToCache(rule, matchName, matchedRule);
                break;
            }
        }
        Number value = getNumberValue(matchedRule, beanValue);
        if (value != null) {
            addSample(new Collector.MetricFamilySamples.Sample(matchedRule.name, matchedRule.labelNames,
                    matchedRule.labelValues, value.doubleValue()), matchedRule.type, matchedRule.help);
        }


    }

    private MatchedRule defaultExport(
            String matchName,
            String domain,
            LinkedHashMap<String, String> beanProperties,
            String attrName,
            Double value,
            double valueFactor) {
        StringBuilder targetName = new StringBuilder().append(domain);
        String srcName = getSourceName(beanProperties);
        String newSrcName = updateSourceName(srcName);
        boolean updateSource = !srcName.equals(newSrcName);

        List<String> labelNames = new ArrayList<>();
        List<String> labelValues = new ArrayList<>();

        String[] sourceItem = newSrcName.split("\\.");
        targetName.append(MetricsConstant.SEP).append(sourceItem[0]);
        boolean methodTag = checkMethodTag(sourceItem[0]);
        if (methodTag) {
            targetName.append(MetricsConstant.SEP).append(sourceItem[1]);
            labelNames.add(MetricsConstant.METHOD);
            StringBuilder sb = new StringBuilder().append(sourceItem[0]).append(".").append(sourceItem[1]).append(".");
            labelValues.add(newSrcName.replaceFirst(sb.toString(), ""));
        } else {
            for (int i = 1; i < sourceItem.length; i++) {
                if (!updateSource && i == 1) {
                    targetName.append(MetricsConstant.SEP).append(sourceItem[i]);
                } else {
                    labelNames.add(MetricsConstant.L + (!updateSource ? i - 1 : i));
                    labelValues.add(sourceItem[i]);
                }
            }
        }

        if (StringUtils.isNotBlank(attrName)) {
            labelNames.add(MetricsConstant.TYPE);
            labelValues.add(attrName);
        }
        String fullName = safeName(targetName.toString());
        addExtTags(labelNames, labelValues);
        return new MatchedRule(fullName, matchName, Collector.Type.UNKNOWN, "", labelNames, labelValues, value, valueFactor);
    }

    private Double getDoubleValue(JmxCollector.Rule rule, Matcher matcher) {
        if (rule.value != null && !rule.value.isEmpty()) {
            String val = matcher.replaceAll(rule.value);
            if (StringUtils.isNumeric(val)) {
                return Double.valueOf(val);
            }
        }
        return null;
    }

    private String getMatchName(JmxCollector.Rule rule, Object beanValue, String beanName, String attrNameSnakeCase, String attrName) {
        Object matchBeanValue = rule.cache ? "<cache>" : beanValue;
        return beanName + (rule.attrNameSnakeCase ? attrNameSnakeCase : attrName) + ": " + matchBeanValue;
    }

    private MatchedRule getMatchedRuleCache(JmxCollector.Rule rule, String matchName) {
        MatchedRule cachedRule = config.getRulesCache().get(rule, matchName);
        if (cachedRule != null) {
            stalenessTracker.add(rule, matchName);
            if (cachedRule.isMatched()) {
                return cachedRule;
            }
        }
        return null;
    }

    private Number getNumberValue(MatchedRule matchedRule, Object beanValue) {
        if (matchedRule.value != null) {
            beanValue = matchedRule.value;
        }

        if (beanValue instanceof Number) {
            return ((Number) beanValue).doubleValue() * matchedRule.valueFactor;
        } else if (beanValue instanceof Boolean) {
            return Boolean.TRUE.equals(beanValue) ? 1 : 0;
        }
        return null;
    }

    private String getSourceName(LinkedHashMap<String, String> beanProperties) {
        String sourceName = "";
        for (Map.Entry<String, String> entry : beanProperties.entrySet()) {
            if (MetricsConstant.NAME.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return sourceName;
    }

    private boolean checkMethodTag(String name) {
        return getConfigProperties().getMethodTagSet().contains(name);
    }

    private boolean checkIgnoreTag(String tagName) {
        return getConfigProperties().getIgnoreTagSet().contains(tagName);
    }


    private String updateSourceName(String sourceName) {
        String dataMap = config.getMetricsConfigProperties().getMetricsMap();
        String[] dataItem = dataMap.split(";");
        for (String name : dataItem) {
            String[] source = name.split("=");
            if (sourceName.contains(source[0])) {
                return sourceName.replace(source[0], source[1]);
            }

        }
        return sourceName;
    }

    private String replaceDomain(String domain) {
        if (StringUtils.isNotBlank(domain) && MetricsConstant.DOMAIN_SOURCE.equals(domain)) {
            return MetricsConstant.DOMAIN_TARGET;
        }
        return domain;
    }


    private void addExtTags(List<String> labelNames, List<String> labelValues) {
        if (config.getLabelNamesValues() == null && config.getLabelNamesValues().size() < 1) {
            return;
        }
        //基础标签
        config.getLabelNamesValues().forEach((key, value) -> {
            labelNames.add(key);
            labelValues.add(value);

        });
        //配置中心配置标签
        if (config.getMetricsConfigProperties() != null && config.getMetricsConfigProperties().getExtTagMap() != null) {
            config.getMetricsConfigProperties().getExtTagMap().forEach((key, value) -> {
                labelNames.add(key);
                labelValues.add(value);

            });
        }
    }


    private String angleBrackets(String s) {
        return "<" + s.substring(1, s.length() - 1) + ">";
    }

    private void addSample(Collector.MetricFamilySamples.Sample sample, Collector.Type type, String help) {
        Collector.MetricFamilySamples mfs = metricFamilySamplesMap.get(sample.name);
        if (mfs == null) {
            mfs = new Collector.MetricFamilySamples(sample.name, type, help, new ArrayList<>());
            metricFamilySamplesMap.put(sample.name, mfs);
        }
        MetricsSampleKey sampleKey = MetricsSampleKey.of(sample);
        if (sampleKeys.contains(sampleKey)) {
            return;
        }
        mfs.samples.add(sample);
        sampleKeys.add(sampleKey);
    }

    private void addToCache(final JmxCollector.Rule rule, final String cacheKey, final MatchedRule matchedRule) {
        if (rule.cache) {
            config.getRulesCache().put(rule, cacheKey, matchedRule);
            stalenessTracker.add(rule, cacheKey);
        }
    }

    private MetricsConfigProperties getConfigProperties() {
        return config.getMetricsConfigProperties();
    }


}