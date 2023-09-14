package cn.iris.cloud.configcenter;


import cn.iris.cloud.configcenter.annotation.ConfigurationProperties;
import cn.iris.cloud.configcenter.dynamic.ConfigChangedEvent;
import cn.iris.cloud.configcenter.utils.ConfigBean;
import cn.iris.cloud.configcenter.utils.ConfigConstant;
import cn.iris.cloud.configcenter.utils.ConfigException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * ConfigLoader
 *
 * @author wuhao
 **/
public class ConfigLoader {
	private static String identityOfBean(Class<?> clazz, String namespace, String prefix) {
		return namespace + "-" + prefix + "-" + clazz.getName();
	}

	private final Cache<String, ConfigBean<?>> beans;

	public ConfigLoader() {
		this.beans = Caffeine.newBuilder().maximumSize(10000).build();
	}

	public static ConfigLoader getInstance() {
		return Holder.instance;
	}

	public <T> T getConfig(Class<T> clazz) {
		return this.getOrCreate(clazz).getBean();
	}

	public <T> T getConfig(Class<T> clazz, String namespace, String prefix) {
		return this.getOrCreate(clazz, namespace, prefix).getBean();
	}

	public <T> void watchConfig(Class<T> clazz, ConfigValidator<T> validator) {
		this.getOrCreate(clazz).register(validator);
	}

	public <T> void unwatchConfig(Class<T> clazz, ConfigValidator<T> validator) {
		this.getOrCreate(clazz).unregister(validator);
	}

	public <T> void watchConfig(Class<T> clazz, String namespace, String prefix, ConfigValidator<T> validator) {
		this.getOrCreate(clazz, namespace, prefix).register(validator);
	}

	public <T> void unwatchConfig(Class<T> clazz, String namespace, String prefix, ConfigValidator<T> validator) {
		this.getOrCreate(clazz, namespace, prefix).unregister(validator);
	}

	private <T> ConfigBean<T> getOrCreate(Class<T> clazz) {
		final ConfigurationProperties prop = clazz.getAnnotation(ConfigurationProperties.class);
		if (prop == null) {
			throw new ConfigException("no ConfigurationProperties specified with " + clazz);
		}
		return this.getOrCreate(clazz, prop.namespace(), prop.prefix());
	}

	@SuppressWarnings("unchecked")
	private <T> ConfigBean<T> getOrCreate(Class<T> clazz, String namespace, String prefix) {
		namespace = this.normalize(namespace);
		prefix = StringUtils.trimToEmpty(prefix);
		String key = identityOfBean(clazz, namespace, prefix);

		String finalNamespace = namespace;
		String finalPrefix = prefix;
		ConfigBean<?> beanRet = this.beans.get(key, key1 -> {
			ConfigBean bean = new ConfigBean<>(clazz, finalNamespace, finalPrefix);
			this.loadConfig(bean, null);
			this.keepBeanUpdated(bean);
			return bean;
		});
		return (ConfigBean<T>) beanRet;
	}

	private boolean loadConfig(ConfigBean<?> bean, ConfigChangedEvent changes) {
		String namespace = bean.getNamespace();
		String prefix = bean.getPrefix();
		Set<String> keys = bean.configKeys();
		Map<String, String> config = ConfigCenter.getInstance().getConfigs(namespace, prefix, keys);
		return bean.tryFillBeanData(config, changes);
	}

	private void keepBeanUpdated(final ConfigBean<?> bean) {
		ConfigCenter.getInstance().watchConfigs(bean.getNamespace(), bean.getPrefix(), bean.configKeys(),
				changes -> this.loadConfig(bean, changes));
	}

	private String normalize(String namespace) {
		if (StringUtils.isEmpty(namespace)) {
			return ConfigConstant.APP;
		}
		return namespace;
	}

	private static class Holder {
		public static ConfigLoader instance = new ConfigLoader();
	}
}
