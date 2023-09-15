package cn.iris.cloud.metrics.config;

import cn.iris.cloud.metrics.utils.Environments;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * MetricsConfig
 *
 * @author wuhao
 * @createTime 2023-06-26
 */
public class MetricsConfigTag {
    private String item;
    private String value;

    public MetricsConfigTag(String item, String value) {
        this.item = item;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getItem() {
        return item;
    }

    public static List<MetricsConfigTag> getTags(MetricsConfigProperties properties) {
        List<MetricsConfigTag> baseTags = new ArrayList<>();
        baseTags.add(new MetricsConfigTag("AppName", properties.getAppName()));
        baseTags.add(new MetricsConfigTag("NodeName", properties.getNodeName()));
        baseTags.add(new MetricsConfigTag("HostIp", properties.getIp()));
        return baseTags;
    }

}
