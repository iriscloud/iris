package cn.iris.cloud.common.extension.factory;

import cn.iris.cloud.common.extension.Adaptive;
import cn.iris.cloud.common.extension.ExtensionFactory;
import cn.iris.cloud.common.extension.ExtensionLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AdaptiveExtensionFactory
 */
@Adaptive
public class AdaptiveExtensionFactory implements ExtensionFactory {

	private final List<ExtensionFactory> factories;

	public AdaptiveExtensionFactory() {
		ExtensionLoader<ExtensionFactory> loader = ExtensionLoader.getExtensionLoader(ExtensionFactory.class);
		List<ExtensionFactory> list = new ArrayList<ExtensionFactory>();
		for (String name : loader.getSupportedExtensions()) {
			list.add(loader.getExtension(name));
		}
		factories = Collections.unmodifiableList(list);
	}

	@Override
	public <T> T getExtension(Class<T> type, String name) {
		for (ExtensionFactory factory : factories) {
			T extension = factory.getExtension(type, name);
			if (extension != null) {
				return extension;
			}
		}
		return null;
	}

}
