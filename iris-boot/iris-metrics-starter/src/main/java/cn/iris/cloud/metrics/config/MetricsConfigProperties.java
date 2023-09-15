package cn.iris.cloud.metrics.config;


import cn.iris.cloud.common.utils.NetUtils;
import cn.iris.cloud.metrics.utils.Environments;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.*;

/**
 * @author wuhao
 * @createTime 2023-06-26
 */
@ConfigurationProperties(prefix = "monitor")
public class MetricsConfigProperties {

	private String ip;
	private int port = 18888;
	private String appName = "appName";
	private String nodeName;
	private boolean openExporter = true;
    private boolean autoReg = true;
	private String consulUrl;
    private String ignoreName;
    private String ignoreType = "DurationUnit,RateUnit,50thPercentile,75thPercentile,Min,Max,StdDev," +
            "75thPercentile,FifteenMinuteRate,FiveMinuteRate,999thPercentile,95thPercentile";

    private String methodTag = "rpc,log,services";
    private String metricsMap = "jvm.memory.pools=jvm_memory_pools;jvm.gc=jvm_memory_gc";

    /**
     * 扩展标签 a=a,b=b
     */
    private String extTag;

    /**
     * keepAliveTime，注册保活时间 ServerCheck interval
     */
    private String keepAliveTime = "60s";

    /**
     * keepAliveExpireTime，过期时间 ServerCheck deregisterCriticalServiceAfter
     */
    private String keepAliveExpireTime = "300s";

    /**
     * regTime，超时时间
     */
    private String dataTimeOut = "5s";

    /**
     * 注册心跳时间 默认50s
     */
    private int regIntervalTime = 50;


	public String getIp() {
		if (StringUtils.isNotBlank(Environments.getNodeIp())){
			return Environments.getNodeIp();
		}
		if (StringUtils.isNotBlank(ip)){
			return ip;
		}
		return NetUtils.getLocalAddress().getHostName();
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getAppName() {
		if (StringUtils.isNotBlank(Environments.getServerApp())){
			return Environments.getServerApp();
		}
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getNodeName() {
		if (StringUtils.isNotBlank(nodeName)){
			return nodeName;
		}
		return getAppName() + UUID.randomUUID();
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public boolean isOpenExporter() {
		return openExporter;
	}

	public void setOpenExporter(boolean openExporter) {
		this.openExporter = openExporter;
	}

	public boolean isAutoReg() {
		return autoReg;
	}

	public void setAutoReg(boolean autoReg) {
		this.autoReg = autoReg;
	}

	public String getConsulUrl() {
		return consulUrl;
	}

	public void setConsulUrl(String consulUrl) {
		this.consulUrl = consulUrl;
	}

	public String getIgnoreName() {
		return ignoreName;
	}

	public void setIgnoreName(String ignoreName) {
		this.ignoreName = ignoreName;
	}

	public String getIgnoreType() {
		return ignoreType;
	}

	public void setIgnoreType(String ignoreType) {
		this.ignoreType = ignoreType;
	}

	public String getMethodTag() {
		return methodTag;
	}

	public void setMethodTag(String methodTag) {
		this.methodTag = methodTag;
	}

	public String getMetricsMap() {
		return metricsMap;
	}

	public void setMetricsMap(String metricsMap) {
		this.metricsMap = metricsMap;
	}

	public String getExtTag() {
		return extTag;
	}

	public void setExtTag(String extTag) {
		this.extTag = extTag;
	}

	public String getKeepAliveTime() {
		return keepAliveTime;
	}

	public void setKeepAliveTime(String keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public String getKeepAliveExpireTime() {
		return keepAliveExpireTime;
	}

	public void setKeepAliveExpireTime(String keepAliveExpireTime) {
		this.keepAliveExpireTime = keepAliveExpireTime;
	}

	public String getDataTimeOut() {
		return dataTimeOut;
	}

	public void setDataTimeOut(String dataTimeOut) {
		this.dataTimeOut = dataTimeOut;
	}

	public int getRegIntervalTime() {
		return regIntervalTime;
	}

	public void setRegIntervalTime(int regIntervalTime) {
		this.regIntervalTime = regIntervalTime;
	}

	public Map<String, String> getExtTagMap() {
        Map<String, String> maps = new HashMap<>();
        if (StringUtils.isNotBlank(getExtTag())) {
            String[] extTags = getExtTag().split(",");
            for (String tagItem : extTags) {
                String[] itemKv = tagItem.split("=");
                if (itemKv.length > 1) {
                    maps.put(itemKv[0], itemKv[1]);
                }
            }
        }
        return maps;
    }

    public Set<String> getIgnoreTagSet() {
        Set<String> strings = new HashSet<>();
        if (StringUtils.isNotBlank(getIgnoreType())) {
            strings.addAll(Arrays.asList(getIgnoreType().split(",")));
        }
        return strings;
    }

    public Set<String> getMethodTagSet() {
        Set<String> strings = new HashSet<>();
        if (StringUtils.isNotBlank(getMethodTag())) {
            strings.addAll(Arrays.asList(getMethodTag().split(",")));
        }
        return strings;
    }

}
