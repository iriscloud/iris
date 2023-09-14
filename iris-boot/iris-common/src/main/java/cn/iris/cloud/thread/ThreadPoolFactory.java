package cn.iris.cloud.thread;

import java.util.concurrent.*;

/**
 * ThreadPoolFactory
 *
 * @author wuhao
 */
public class ThreadPoolFactory {

    private static final ConcurrentMap<String, ExecutorService> THREAD_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, ScheduledExecutorService> THREAD_MAP_SCHEDULED =
        new ConcurrentHashMap<>();

    private ThreadPoolFactory() {}

    public static ExecutorService getThreadPool(String name) {
        return getThreadPool(name, Runtime.getRuntime().availableProcessors());
    }

    /**
     * getThreadPool
     * 
     * @param name 线程池的名字,
     * @param coreSize 最大数量. 必须> 1
     */
    public static ExecutorService getThreadPool(String name, int coreSize) {
        return getThreadPool(name, coreSize, 0);
    }

    /**
     * getThreadPool
     * 
     * @param name 线程池的名字,
     * @param coreSize 最大数量. 必须> 1
     * @param workerQueueSize 线程队列数量, 当 workerQueueSize <=0 workerQueueSize:使用默认值 Integer.MAX_VALUE
     */
    public static ExecutorService getThreadPool(String name, int coreSize, int workerQueueSize) {
        return getThreadPool(name, coreSize, coreSize * 2, workerQueueSize, new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * getThreadPool
     * 
     * @param name 线程池的名字,
     * @param coreSize 线程数量. 必须> 1
     * @param maxThreadSize 最大数量. 必须> 1 ,并且大于 coreSize ,否则使用coreSize
     * @param workerQueueSize 线程队列数量, 当 workerQueueSize <=0 workerQueueSize:使用默认值 Integer.MAX_VALUE
     * @param handler 拒绝策略, 如果为空 使用 ThreadPoolExecutor.AbortPolicy()
     */
    public static ExecutorService getThreadPool(String name, int coreSize, int maxThreadSize, int workerQueueSize,
        RejectedExecutionHandler handler) {
        return THREAD_MAP.computeIfAbsent(name,
            n -> new ObservableThreadPool(name, coreSize, maxThreadSize, workerQueueSize, handler));
    }

    public static ExecutorService getSingleThreadExecutor(String name) {
        return THREAD_MAP.computeIfAbsent(name, n -> {
            ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(name).setDaemon(false).build();
            return Executors.newSingleThreadExecutor(factory);
        });
    }

    public static ExecutorService getFixThreadExecutor(String name, int size) {
        return THREAD_MAP.computeIfAbsent(name, n -> {
            ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(name).setDaemon(false).build();
            return new ThreadPoolExecutor(size, size, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                factory);
        });
    }

    public static ScheduledExecutorService getSingleScheduledThreadPoolExecutor(String name) {
        return THREAD_MAP_SCHEDULED.computeIfAbsent(name, n -> {
            ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(name).setDaemon(true).build();
            return new ScheduledThreadPoolExecutor(1, factory);
        });
    }

    public static ScheduledExecutorService getScheduledThreadPool(String name, int coreSize) {
        return THREAD_MAP_SCHEDULED.computeIfAbsent(name, n -> {
            ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat(name + "_%d").setDaemon(true).build();
            return new ScheduledThreadPoolExecutor(coreSize, factory);
        });
    }
}
