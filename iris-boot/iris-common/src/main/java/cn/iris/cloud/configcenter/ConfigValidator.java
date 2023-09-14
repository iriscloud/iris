package cn.iris.cloud.configcenter;

/**
 * ConfigValidator
 *
 * @param <T>
 */
@FunctionalInterface
public interface ConfigValidator<T> {
  boolean validate(T changing);
}