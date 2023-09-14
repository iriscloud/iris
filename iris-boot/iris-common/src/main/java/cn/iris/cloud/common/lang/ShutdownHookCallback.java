package cn.iris.cloud.common.lang;

import cn.iris.cloud.common.extension.SPI;

/**
 * cloud ShutdownHook callback interface
 *
 * @since 2.7.5
 */
@SPI
public interface ShutdownHookCallback extends Prioritized {

	/**
	 * Callback execution
	 *
	 * @throws Throwable if met with some errors
	 */
	void callback() throws Throwable;
}
