package cn.iris.cloud.common.status;

import cn.iris.cloud.common.extension.SPI;

/**
 * StatusChecker
 */
@SPI
public interface StatusChecker {

	/**
	 * check status
	 *
	 * @return status
	 */
	Status check();

}