package cn.iris.cloud.common.extension.factory;

import cn.iris.cloud.common.extension.ExtensionFactory;
import cn.iris.cloud.common.extension.ExtensionLoader;
import cn.iris.cloud.common.extension.SPI;

/**
 * SpiExtensionFactory
 */
public class SpiExtensionFactory implements ExtensionFactory {

	@Override
	public <T> T getExtension(Class<T> type, String name) {
		if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
			ExtensionLoader<T> loader = ExtensionLoader.getExtensionLoader(type);
			if (!loader.getSupportedExtensions().isEmpty()) {
				return loader.getAdaptiveExtension();
			}
		}
		return null;
	}

}
