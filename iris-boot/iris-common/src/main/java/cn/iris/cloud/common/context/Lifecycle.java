package cn.iris.cloud.common.context;

/**
 * The Lifecycle of cloud component
 *
 * @since 2.7.5
 */
public interface Lifecycle {

	/**
	 * Initialize the component before {@link #start() start}
	 *
	 * @return current {@link Lifecycle}
	 * @throws IllegalStateException
	 */
	void initialize() throws IllegalStateException;

	/**
	 * Start the component
	 *
	 * @return current {@link Lifecycle}
	 * @throws IllegalStateException
	 */
	void start() throws IllegalStateException;

	/**
	 * Destroy the component
	 *
	 * @throws IllegalStateException
	 */
	void destroy() throws IllegalStateException;
}
