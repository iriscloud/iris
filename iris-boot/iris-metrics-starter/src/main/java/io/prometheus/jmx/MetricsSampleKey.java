package io.prometheus.jmx;

import io.prometheus.client.Collector;

import java.util.List;
import java.util.Objects;

/**
 * MetricsSampleKey
 *
 * @author wuhao
 */
public class MetricsSampleKey {
    private final String name;
    private final List<String> labelNames;
    private final List<String> labelValues;

    public MetricsSampleKey(String name, List<String> labelNames, List<String> labelValues) {
        this.name = name;
        this.labelNames = labelNames;
        this.labelValues = labelValues;
    }

    public static MetricsSampleKey of(Collector.MetricFamilySamples.Sample sample) {
        return new MetricsSampleKey(sample.name, sample.labelNames, sample.labelValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MetricsSampleKey sampleKey = (MetricsSampleKey) o;

        if (!Objects.equals(name, sampleKey.name)) {
            return false;
        }
        if (!Objects.equals(labelValues, sampleKey.labelValues)) {
            return false;
        }

        return Objects.equals(labelNames, sampleKey.labelNames);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (labelNames != null ? labelNames.hashCode() : 0);
        result = 31 * result + (labelValues != null ? labelValues.hashCode() : 0);
        return result;
    }

}