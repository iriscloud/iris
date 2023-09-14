package cn.iris.cloud.thread;

import cn.iris.cloud.metrics.MetricsFactory;
import cn.iris.cloud.metrics.api.MetricsMeter;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * ObservableLinkedBlockingQueue
 */
public class ObservableLinkedBlockingQueue<E> extends LinkedBlockingQueue<E>
                                              implements DetectableBlockQueue {
    private static final long serialVersionUID = 1618319324404357384L;

    private final int capacity;
    private final String name;

    private MetricsMeter enqueue;
    private MetricsMeter error;

    public ObservableLinkedBlockingQueue(String name) {
        this(Integer.MAX_VALUE, name);
    }

    public ObservableLinkedBlockingQueue(int capacity, String name) {
        super(capacity);
        this.capacity = capacity;
        this.name = name;
        initMetrics();
    }

    public ObservableLinkedBlockingQueue(Collection<? extends E> c, String name) {
        super(c);
        this.capacity = Integer.MAX_VALUE;
        this.name = name;
        initMetrics();
    }

    private void initMetrics() {
        MetricsFactory.gauge(this::size, ThreadConstant.PREFIX_QUEUE, this.name, ThreadConstant.NAME_SIZE);
        this.enqueue = MetricsFactory.meter(ThreadConstant.PREFIX_QUEUE, this.name, ThreadConstant.NAME_IN);
        this.error = MetricsFactory.meter(ThreadConstant.PREFIX_QUEUE, this.name, ThreadConstant.NAME_FAIL);
    }

    @Override
    public boolean add(E e) {
        try {
            this.enqueue.mark();
            return super.add(e);
        } catch (RuntimeException e2) {
            this.error.mark();
            throw e2;
        }
    }

    @Override
    public void put(E e) throws InterruptedException {
        try {
            this.enqueue.mark();
            super.put(e);
        } catch (Exception ex) {
            this.error.mark();
            throw ex;
        }
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        try {
            this.enqueue.mark();
            boolean result = super.offer(e, timeout, unit);
            if (!result) {
                this.error.mark();
            }
            return result;
        } catch (Exception ex) {
            this.error.mark();
            throw ex;
        }
    }

    @Override
    public boolean offer(E e) {
        try {
            this.enqueue.mark();
            boolean result = super.offer(e);
            if (!result) {
                this.error.mark();
            }
            return result;
        } catch (RuntimeException e2) {
            this.error.mark();
            throw e2;
        }
    }

    @Override
    public int getQueueSize() {
        return super.size();
    }

    @Override
    public int getQueueCapacity() {
        return capacity;
    }

    @Override
    public String getName() {
        return name;
    }
}
