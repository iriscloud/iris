package cn.iris.cloud.common.compiler.support;

import cn.iris.cloud.common.compiler.Compiler;
import cn.iris.cloud.common.extension.Adaptive;
import cn.iris.cloud.common.extension.ExtensionLoader;

/**
 * AdaptiveCompiler. (SPI, Singleton, ThreadSafe)
 */
@Adaptive
public class AdaptiveCompiler implements Compiler {

	private static volatile String DEFAULT_COMPILER;

	public static void setDefaultCompiler(String compiler) {
		DEFAULT_COMPILER = compiler;
	}

	@Override
	public Class<?> compile(String code, ClassLoader classLoader) {
		Compiler compiler;
		ExtensionLoader<Compiler> loader = ExtensionLoader.getExtensionLoader(Compiler.class);
		String name = DEFAULT_COMPILER; // copy reference
		if (name != null && name.length() > 0) {
			compiler = loader.getExtension(name);
		} else {
			compiler = loader.getDefaultExtension();
		}
		return compiler.compile(code, classLoader);
	}

}
