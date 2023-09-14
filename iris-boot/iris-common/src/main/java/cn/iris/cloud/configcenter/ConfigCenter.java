package cn.iris.cloud.configcenter;

import cn.iris.cloud.common.extension.ExtensionLoader;
import cn.iris.cloud.common.utils.ConfigUtils;
import cn.iris.cloud.configcenter.configmanager.ConfigManager;
import cn.iris.cloud.configcenter.configmanager.FileConfigManager;
import cn.iris.cloud.configcenter.dynamic.ConfigurationListener;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ConfigCenter
 *
 * @author wuhao
 **/
public class ConfigCenter {
	public static ConfigCenter getInstance() {
		return ConfigCenterFactory.getInstance("default");
	}

	private ConfigManager manager;
	private boolean registerClosed = false;

	public ConfigCenter() {
		manager = ExtensionLoader.loadOrByDefaultFactory(ConfigManager.class,
				ConfigUtils.getProperty(ConfigManager.KEY, ConfigManager.DEFAULT), FileConfigManager::new);
	}

	public ConfigCenter(ConfigManager manager) {
		this.manager = manager;
	}

	/**
	 * 关闭配置注册
	 */
	public void closeRegister() {
		this.registerClosed = true;
	}


	/**
	 * 通过默认(应用)命名空间获取配置
	 */
	public String getConfig(String configKey) {
		return this.getConfig(null, configKey);
	}

	/**
	 * 获取配置
	 */
	public String getConfig(String namespace, String configKey) {
		String property = this.manager.getConfig(namespace, configKey);
		return property;
	}

	/**
	 * getConfigWithDefault
	 *
	 * @param configKey
	 * @return
	 */
	public String getConfigWithDefault(String configKey, String defaultValue) {
		return this.getConfigWithDefault(null, configKey, defaultValue);
	}

	/**
	 * getConfigWithDefault
	 *
	 * @param namespace
	 * @param configKey
	 * @param defaultValue
	 * @return
	 */
	public String getConfigWithDefault(String namespace, String configKey, String defaultValue) {
		String configValue = this.getConfig(namespace, configKey);
		if (StringUtils.isEmpty(configValue)) {
			return defaultValue;
		}
		return configValue;
	}

	public Map<String, String> getConfigs(String namespace, Set<String> configKeys) {
		return this.getConfigs(namespace, null, configKeys);
	}

	public Map<String, String> getConfigs(String namespace, String prefix, Set<String> configKeys) {
		return this.getConfigs(namespace, prefix, (Collection<String>) configKeys);
	}

	private Map<String, String> getConfigs(String namespace, String prefix, Collection<String> configKeys) {
		final Map<String, String> result = new HashMap<>(configKeys.size() + 2);
		for (String cfg : configKeys) {
			String key = cfg;
			if (StringUtils.isNotBlank(prefix)) {
				key = prefix + "." + key;
			}
			String value = this.getConfig(namespace, key);
			if (!StringUtils.isEmpty(value)) {
				result.put(cfg, value);
			}
		}
		return result;
	}

	public Map<String, String> loadConfigs(String namespace) {
		return this.loadConfigs(namespace, null);
	}

	public Map<String, String> loadConfigs(String namespace, String prefix) {
		Set<String> configKeys = this.manager.keySet(namespace);
		if (StringUtils.isNotBlank(prefix)) {
			final String prefixDot = prefix + ".";
			configKeys = configKeys.stream().filter(k -> k.startsWith(prefixDot) && k.length() > prefixDot.length())
					.map(k -> k.substring(prefixDot.length())).collect(Collectors.toSet());
		}
		return this.getConfigs(namespace, prefix, (Collection<String>) configKeys);
	}

	public void watchConfigs(String namespace, ConfigurationListener watcher) {
		this.manager.watchConfigs(namespace, watcher);
	}

	public void watchConfigs(String namespace, String prefix, Set<String> configKeys, ConfigurationListener watcher) {
		if (StringUtils.isNotBlank(prefix)) {
			configKeys = configKeys.stream().map(k -> prefix + "." + k).collect(Collectors.toSet());
		}
		this.manager.watchConfigs(namespace, watcher, configKeys);
	}

	public void unwatchConfigs(String namespace, ConfigurationListener watcher) {
		this.manager.unwatchConfigs(namespace, watcher);
	}

	public ConfigManager getManager() {
		return manager;
	}

}
