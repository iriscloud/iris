package cn.iris.cloud.configcenter.configmanager;


import cn.iris.cloud.common.extension.SPI;
import cn.iris.cloud.configcenter.dynamic.ConfigurationListener;

import java.util.Set;

/**
 * ConfigManager
 *
 * @author wuhao
 */
@SPI
public interface ConfigManager {

	String KEY = "ConfigManager";
	String DEFAULT = "file";

	/**
	 * keySet
	 *
	 * @param namespace
	 * @return
	 */
	Set<String> keySet(String namespace);

	/**
	 * getConfig
	 *
	 * @param namespace
	 * @param key
	 * @return
	 */
	String getConfig(String namespace, String key);

	/**
	 * watchConfigs
	 *
	 * @param namespace
	 * @param watcher
	 */
	void watchConfigs(String namespace, ConfigurationListener watcher);

	/**
	 * watchConfigs
	 *
	 * @param namespace
	 * @param watcher
	 * @param keys
	 */
	void watchConfigs(String namespace, ConfigurationListener watcher, Set<String> keys);

	/**
	 * unwatchConfigs
	 *
	 * @param namespace
	 * @param watcher
	 */
	void unwatchConfigs(String namespace, ConfigurationListener watcher);


}
