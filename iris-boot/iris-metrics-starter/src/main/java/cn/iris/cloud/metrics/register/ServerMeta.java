package cn.iris.cloud.metrics.register;

/**
 * ServerMeta
 *
 * @author wuhao
 */

public class ServerMeta {
    private String version = "1.0";
    private String app = "app";

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}
