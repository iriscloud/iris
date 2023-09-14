package cn.iris.cloud.configcenter.utils;

import cn.iris.cloud.configcenter.ConfigValidator;
import cn.iris.cloud.configcenter.annotation.ConfigurationProperties;
import cn.iris.cloud.configcenter.convert.ConverterService;
import cn.iris.cloud.configcenter.dynamic.ConfigChangedEvent;
import cn.iris.cloud.configcenter.dynamic.ConfigurationListener;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ConfigBean
 *
 * @param <T>
 */
public class ConfigBean<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigBean.class);

    private static final int IGNORE_FIELD = Modifier.FINAL | Modifier.STATIC;

    private static boolean trySetAccessible(final Field field) {
        try {
            field.setAccessible(true);
            return true;
        } catch (RuntimeException e) {
            if (e instanceof SecurityException) {
                throw e;
            }
            return false;
        }
    }

    private static Map<String, Field> getAllFields(final Class<?> clazz) {
        final Map<String, Field> fields = Stream
                .of(clazz.getDeclaredFields())
                .filter(f -> !f.isSynthetic()
                        && !f.isEnumConstant()
                        && (f.getModifiers() & IGNORE_FIELD) == 0
                        && !f.isAnnotationPresent(ConfigurationProperties.Ignore.class)
                        && trySetAccessible(f))
                .collect(Collectors.toMap(Field::getName, f -> f));
        final Class<?> supzz = clazz.getSuperclass();
        if (supzz != null && supzz != clazz) {
            getAllFields(supzz).forEach(fields::putIfAbsent);
        }
        return fields;
    }

    private final Set<ConfigValidator<T>> validators;
    private final ConverterService converter;
    private final Class<T> clazz;
    private final Constructor<T> ctor;
    private final T stable;
    private final List<Field> fields;
    private final Map<String, Object> configs;

    private final String namespace;
    private final String prefix;
    private final boolean ignoreInvalidFields;
    private final boolean ignoreUnknownFields;

    public ConfigBean(final Class<T> clazz, final String namespace, final String prefix) {
        this.validators = ConcurrentHashMap.newKeySet();
        this.converter = ConverterService.getInstance();
        this.clazz = clazz;
        try {
            this.ctor = clazz.getDeclaredConstructor();
            this.ctor.setAccessible(true);
            this.stable = this.ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ConfigException(e);
        }

        final Map<String, Field> fields = getAllFields(clazz);
        this.fields = Lists.newArrayList(fields.values());
        this.configs = Maps.transformValues(fields, f -> {
            try {
                return f.get(this.stable);
            } catch (ReflectiveOperationException e) {
                throw new ConfigException(e);
            }
        });

        this.namespace = namespace;
        this.prefix = prefix;
        final ConfigurationProperties prop = clazz.getAnnotation(ConfigurationProperties.class);
        this.ignoreInvalidFields = prop == null || prop.ignoreInvalidFields();
        this.ignoreUnknownFields = prop == null || prop.ignoreUnknownFields();
    }

    public T getBean() {
        return this.stable;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public Set<String> configKeys() {
        return this.configs.keySet();
    }

    public void register(final ConfigValidator<T> validator) {
        this.validators.add(validator);
    }

    public void unregister(final ConfigValidator<T> validator) {
        this.validators.remove(validator);
    }

    public boolean tryFillBeanData(final Map<String, String> config, final ConfigChangedEvent changes) {
        if (config == null || config.isEmpty()) {
            if (!this.ignoreUnknownFields) {
                throw new ConfigNotFoundException("Config NotFound: " + this.clazz);
            }
            return false;
        }

        T changing;
        try {
            changing = this.ctor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ConfigException(e);
        }
        this.fillBeanData(changing, config);
        for (final ConfigValidator<T> validator : this.validators) {
            if (!validator.validate(changing)) {
                return false;
            }
        }

        this.fillBeanData(this.stable, config);
        if (changes != null) {
            if (this.stable instanceof ConfigurationListener) {
                ((ConfigurationListener) this.stable).process(changes);
            }
            this.validators.forEach(n -> {
                if (n instanceof ConfigurationListener){
					((ConfigurationListener) n).process(changes);
				}
            });
        }
        return true;
    }

    private void fillBeanData(final T bean, final Map<String, String> config) {
        for (final Field field : this.fields) {
            final String key = field.getName();
            final String value = config.get(key);
            if (StringUtils.isBlank(value)) {
                if (!this.ignoreUnknownFields) {
                    throw new ConfigNotFoundException("Config NotFound: " + this.clazz + "::" + key);
                }
                try {
                    field.set(bean, this.configs.get(key));
                } catch (ReflectiveOperationException e) {
                    throw new ConfigException(e);
                }
            } else {
                fillFieldData(bean, field, value);
            }
        }
    }

    private void fillFieldData(final T bean, final Field field, final String attValue) {
        try {
            if (int.class == field.getType()) {
                field.setInt(bean, Integer.valueOf(attValue));
            } else if (Integer.class == field.getType()) {
                field.set(bean, Integer.valueOf(attValue));
            } else if (long.class == field.getType()) {
                field.setLong(bean, Long.valueOf(attValue));
            } else if (Long.class == field.getType()) {
                field.set(bean, Long.valueOf(attValue));
            } else if (String.class == field.getType()) {
                field.set(bean, attValue);
            } else if (double.class == field.getType()) {
                field.setDouble(bean, Double.valueOf(attValue));
            } else if (Double.class == field.getType()) {
                field.set(bean, Double.valueOf(attValue));
            } else if (float.class == field.getType()) {
                field.setFloat(bean, Float.valueOf(attValue));
            } else if (Float.class == field.getType()) {
                field.set(bean, Float.valueOf(attValue));
            } else {
                if (boolean.class == field.getType() || Boolean.class == field.getType()) {
                    field.set(bean, this.converter.convert(Boolean.class, attValue));
                } else if (char.class == field.getType() || Character.class == field.getType()) {
                    field.setChar(bean, this.converter.convert(Character.class, attValue));
                } else {
                    final Object o = this.converter
                        .convert(field.getType(), field.getGenericType(), attValue);
                    if (o != null) {
                        field.set(bean, o);
                    }
                }
            }
        } catch (NumberFormatException e) {
            if (!this.ignoreInvalidFields) {
                LOGGER.warn(
                        "class={} field={} attValue={}   isInvalidFields use default",
                        this.clazz, field.getName(), attValue, e);
            }
        } catch (ReflectiveOperationException e) {
            throw new ConfigException(e);
        }
    }
}