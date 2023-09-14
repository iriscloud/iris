package cn.iris.cloud.thread.spi;


/**
 * IObservableThreadExecute
 *
 * @author wuhao
 */
public interface IObservableThreadExt {
    /**
     * 更新Runnable
     *
     * @param runnable
     * @return
     */
    Runnable refreshRunnable(Runnable runnable);
}
