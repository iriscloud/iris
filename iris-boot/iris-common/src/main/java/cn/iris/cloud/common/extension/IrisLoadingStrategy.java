package cn.iris.cloud.common.extension;

/**
 * cloud {@link LoadingStrategy}
 *
 * @since 2.7.7
 */
public class IrisLoadingStrategy implements LoadingStrategy {

	@Override
	public String directory() {
		return "META-INF/iris/";
	}

	@Override
	public boolean overridden() {
		return true;
	}

	@Override
	public int getPriority() {
		return NORMAL_PRIORITY;
	}


}
