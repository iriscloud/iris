package cn.iris.cloud.configcenter.utils;

/**
 * ConfigException
 *
 * @author wuhao
 */
public class ConfigException extends RuntimeException {
    private static final long serialVersionUID = 2424924707471297822L;

    public ConfigException() {
        super();
    }

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigException(Throwable cause) {
        super(cause);
    }

    protected ConfigException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
