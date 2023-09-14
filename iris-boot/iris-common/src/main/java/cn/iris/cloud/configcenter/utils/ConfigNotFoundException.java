package cn.iris.cloud.configcenter.utils;

/**
 * ConfigNotFoundException
 * @author wuhao
 **/
public class ConfigNotFoundException extends ConfigException {
    private static final long serialVersionUID = 460285596014728210L;

    private Throwable ex;

    public ConfigNotFoundException() {
        super((Throwable) null);
    }

    public ConfigNotFoundException(String s) {
        super(s, null);
    }

    public ConfigNotFoundException(String s, Throwable ex) {
        super(s, null);
        this.ex = ex;
    }

    public Throwable getException() {
        return ex;
    }

    @Override
    public Throwable getCause() {
        return ex;
    }
}
