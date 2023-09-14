package cn.iris.cloud.common.extension;

/**
 * cloud internal {@link LoadingStrategy}
 *
 * @since 2.7.7
 */
public class IrisInternalLoadingStrategy implements LoadingStrategy {

	@Override
	public String directory() {
		return "META-INF/iris/internal/";
	}

	@Override
	public int getPriority() {
		return MAX_PRIORITY;
	}
}
