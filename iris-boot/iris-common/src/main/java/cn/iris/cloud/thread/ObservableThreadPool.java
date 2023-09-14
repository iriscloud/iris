package cn.iris.cloud.thread;

import cn.iris.cloud.metrics.MetricsFactory;
import cn.iris.cloud.metrics.api.MetricsMeter;
import cn.iris.cloud.metrics.api.MetricsTimer;
import cn.iris.cloud.thread.spi.IObservableThreadExt;
import cn.iris.cloud.thread.spi.ObservableThreadExtNop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 一个可以看到工作队列消费情况的执行器
 * * <pre>
 *  * +----------------------------------------------+---------------+
 *  *                     监控项                         是否周期数据
 *  * 当前状态(积压数量)                                   否
 *  * 计数 (入队总数量,入队失败次数)                         否
 *  * 当前采集周期入队数量                                  是
 *  * 当前采集周期入队失败数量                               是
 *  * +----------------------------------------------+---------------+
 *  * </pre>
 * *
 *
 */
public class ObservableThreadPool extends ThreadPoolExecutor implements DetectableBlockQueue {
	private static final Logger LOGGER = LoggerFactory.getLogger(ObservableThreadPool.class);

	private final String name;
	private final int queueCapacity;
	private final BlockingQueue<Runnable> workerQueue;

	private MetricsTimer running;
	private MetricsMeter error;
	private IObservableThreadExt observableThreadExt = new ObservableThreadExtNop();
	private ObservableThreadPool(String name, int queueCapacity,
								 int corePoolSize, int maximumPoolSize,
								 long keepAliveTime, TimeUnit unit,
								 BlockingQueue<Runnable> workerQueue,
								 ThreadFactory factory,
								 RejectedExecutionHandler handler) {
		super(corePoolSize, maximumPoolSize,
			keepAliveTime, unit,
			workerQueue, factory, handler);
		this.name = name;
		this.queueCapacity = queueCapacity;
		this.workerQueue = workerQueue;
		initMetrics(name, workerQueue);
	}

	public ObservableThreadPool(String name,
								int coreSize, int maxThreadSize,
								int queueCapacity,
								RejectedExecutionHandler rejectedHandler) {
		this(name, queueCapacity,
			coreSize, Math.max(coreSize, maxThreadSize),
			0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(
				queueCapacity > 0 ? queueCapacity : Integer.MAX_VALUE),
			new ThreadFactoryBuilder().setNameFormat(name + "_%d").build(),
			rejectedHandler == null ? new AbortPolicy() : rejectedHandler);
	}

	@Override
	public int getQueueSize() {
		return this.workerQueue.size();
	}

	@Override
	public int getQueueCapacity() {
		return this.queueCapacity;
	}

	@Override
	public String getName() {
		return this.name;
	}


	private void initMetrics(String name, BlockingQueue<Runnable> workerBlockingQueue) {
		MetricsFactory.gauge(this::getQueueSize, ThreadConstant.PREFIX_THREAD_POOL, name, ThreadConstant.NAME_SIZE);
		MetricsFactory.gauge(this::getActiveCount, ThreadConstant.PREFIX_THREAD_POOL, name, ThreadConstant.NAME_THREAD_ACTIVE_COUNT);
		MetricsFactory.gauge(this::getPoolSize, ThreadConstant.PREFIX_THREAD_POOL, name, ThreadConstant.NAME_THREAD_POOL_SIZE);
		this.running = MetricsFactory.timer(ThreadConstant.PREFIX_THREAD_POOL, name, ThreadConstant.NAME_THREAD_EXEC);
		this.error = MetricsFactory.meter(ThreadConstant.PREFIX_THREAD_POOL, name, ThreadConstant.NAME_THREAD_SUBMIT_FAIL);
	}

	@Override
	public void execute(Runnable command) {
		try {
			super.execute(RunnableWrapper.of(command, this.running, this.observableThreadExt));
		} catch (Exception e) {
			this.error.mark();
			LOGGER.error("{} thread throw exception ", this.getName(), e);
			throw e;
		}
	}

	public static class RunnableWrapper implements Runnable {

		Runnable command;
		MetricsTimer running;
		IObservableThreadExt observableThreadExt;

		private RunnableWrapper(Runnable command, MetricsTimer running,
								IObservableThreadExt observableThreadExt) {
			this.command = command;
			this.running = running;
			this.observableThreadExt = observableThreadExt;
		}

		static RunnableWrapper of(Runnable command, MetricsTimer running,
								  IObservableThreadExt observableThreadExt) {
			return new RunnableWrapper(command, running, observableThreadExt);
		}

		public Runnable getRunnable() {
			return command;
		}

		public <T extends Runnable> T getCommand() {
			return (T) command;
		}

		@Override
		public void run() {
			this.running.time(this.observableThreadExt.refreshRunnable(command));
		}
	}

}