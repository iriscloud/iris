package cn.iris.cloud.metrics.register;

/**
 * ServerCheck
 *
 * @author wuhao
 */

public class ServerCheck {
    /**
     * 60s
     */
    private String deregisterCriticalServiceAfter = "300s";
    /**
     * ["/usr/local/bin/check_redis.py"]
     */
    private String http;

    /**
     * "50s"
     */
    private String interval = "30s";


    /**
     * "5s"
     */
    private String timeout = "5s";
    

    public String getDeregisterCriticalServiceAfter() {
        return deregisterCriticalServiceAfter;
    }

    public void setDeregisterCriticalServiceAfter(String deregisterCriticalServiceAfter) {
        this.deregisterCriticalServiceAfter = deregisterCriticalServiceAfter;
    }


    public String getHttp() {
        return http;
    }

    public void setHttp(String http) {
        this.http = http;
    }

    public String getInterval() {
        return interval;
    }

    public ServerCheck setInterval(String interval) {
        this.interval = interval;
        return this;
    }

    public String getTimeout() {
        return timeout;
    }

    public ServerCheck setTimeout(String timeout) {
        this.timeout = timeout;
        return this;
    }
}
