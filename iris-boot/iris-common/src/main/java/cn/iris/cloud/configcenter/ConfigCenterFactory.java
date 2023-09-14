package cn.iris.cloud.configcenter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * ConfigCenter
 *
 * @author wuhao
 **/
public class ConfigCenterFactory {
	private static final Cache<String, ConfigCenter> CONFIG_CACHE = Caffeine.newBuilder().maximumSize(10000).build();;
	public static ConfigCenter getInstance(String instance) {
		return CONFIG_CACHE.get(instance, key-> new ConfigCenter());
	}
	public static void put(String config, ConfigCenter configCenter){
		CONFIG_CACHE.put(config, configCenter);
	}

}
