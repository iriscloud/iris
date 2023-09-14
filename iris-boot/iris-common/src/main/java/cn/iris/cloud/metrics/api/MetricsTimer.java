package cn.iris.cloud.metrics.api;

import java.util.concurrent.Callable;
import java.util.function.LongConsumer;

/**
 * MetricsTimer
 *
 * @author wuhao
 */

public interface MetricsTimer {
	/**
	 * timer
	 *
	 * @return
	 */
	MetricsContext time();

	/**
	 * time
	 *
	 * @param elapsed
	 * @return
	 */
	MetricsContext time(LongConsumer elapsed);

	/**
	 * time
	 *
	 * @param event
	 */
	void time(Runnable event);

	/**
	 * time
	 *
	 * @param event
	 * @param elapsed
	 */
	void time(Runnable event, LongConsumer elapsed);

	/**
	 * time
	 *
	 * @param event
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	<T> T time(Callable<T> event) throws Exception;

	/**
	 * time
	 *
	 * @param event
	 * @param elapsed
	 * @param <T>
	 * @return
	 * @throws Exception
	 */
	<T> T time(Callable<T> event, LongConsumer elapsed) throws Exception;

}
