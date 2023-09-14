package cn.iris.cloud.configcenter.annotation;

import java.lang.annotation.*;

/**
 * ConfigurationProperties
 * 
 * @author wuhao
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationProperties {

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Ignore { }

    String prefix() default "";

    String namespace() default "app";

    boolean ignoreInvalidFields() default false;

    boolean ignoreUnknownFields() default true;

}
