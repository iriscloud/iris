package cn.iris.cloud.configcenter.configmanager;


import cn.iris.cloud.common.utils.ConfigUtils;
import cn.iris.cloud.configcenter.dynamic.ConfigurationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

/**
 * FileConfigManager
 * @author wuhao
 */
public class FileConfigManager implements ConfigManager {
	protected static final Logger LOGGER = LoggerFactory.getLogger(FileConfigManager.class);
	private Properties properties;
	public static final String CONFIG = "config";
	public static final String CONFIG_PATH = "CONFIG_PATH";

	@Override
	public Set<String> keySet(String namespace) {
		return getProperties().stringPropertyNames();
	}

	public FileConfigManager() {
		this.properties = loadProperties();
	}

	@Override
	public String getConfig(String namespace, String key) {
		String value = getProperties().getProperty(key);
		return value;
	}

	@Override
	public void watchConfigs(String namespace, ConfigurationListener watcher) {
	}

	@Override
	public void watchConfigs(String namespace, ConfigurationListener watcher, Set<String> keys) {
	}

	@Override
	public void unwatchConfigs(String namespace, ConfigurationListener watcher) {
	}

	public Properties getProperties() {
		if (properties == null) {
			loadProperties();
		}
		return properties;
	}

	public Properties loadProperties() {
		// 默认
		String config = ConfigUtils.getProperty(CONFIG_PATH, CONFIG);
		try {
			return reloadProperties(config);
		} catch (Exception e) {
			LOGGER.error("loadProperties Error:{}", config, e);
		}
		return null;
	}

	public Properties reloadProperties(String path) throws IOException {
		Properties propertiesTmp = new Properties();
		File file = new File(path);
		File[] filesApp = file.listFiles();
		if (filesApp != null && filesApp.length > 0) {
			for (File fileAppItem : filesApp) {
				if (!fileAppItem.isDirectory()) {
					// 一级目录文件
					Properties propertiesTmpItem = new Properties();
					try (FileInputStream fileInputStream = new FileInputStream(fileAppItem)) {
						propertiesTmpItem.load(fileInputStream);
					}
					propertiesTmp.putAll(propertiesTmpItem);
					continue;
				}
				File[] files = fileAppItem.listFiles();
				for (File fileItem : files) {
					// 二级目录文件
					Properties propertiesTmpItem = new Properties();
					try (FileInputStream fileInputStream = new FileInputStream(fileItem)) {
						propertiesTmpItem.load(fileInputStream);
					}
					propertiesTmp.putAll(propertiesTmpItem);
				}
			}
		}
		properties = propertiesTmp;
		return properties;
	}
}
