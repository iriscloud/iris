package cn.iris.cloud.configcenter.dynamic;

import cn.iris.cloud.common.URL;
import cn.iris.cloud.common.extension.ExtensionLoader;
import cn.iris.cloud.common.extension.SPI;

/**
 * The factory interface to create the instance of {@link DynamicConfiguration}
 */
@SPI("nop") // 2.7.5 change the default SPI implementation
public interface DynamicConfigurationFactory {

	DynamicConfiguration getDynamicConfiguration(URL url);

	/**
	 * Get an instance of {@link DynamicConfigurationFactory} by the specified name. If not found, take the default
	 * extension of {@link DynamicConfigurationFactory}
	 *
	 * @param name the name of extension of {@link DynamicConfigurationFactory}
	 * @return non-null
	 * @see 2.7.4
	 */
	static DynamicConfigurationFactory getDynamicConfigurationFactory(String name) {
		Class<DynamicConfigurationFactory> factoryClass = DynamicConfigurationFactory.class;
		ExtensionLoader<DynamicConfigurationFactory> loader = ExtensionLoader.getExtensionLoader(factoryClass);
		return loader.getOrDefaultExtension(name);
	}
}
