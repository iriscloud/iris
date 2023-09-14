package cn.iris.cloud.thread;

/**
 * DetectableBlockQueue
 */
public interface DetectableBlockQueue {

    /**
     * 获取queue的余量
     */
    int getQueueSize();

    /**
     * 获取 queue 的数量限制
     *
     * @return queue capacity
     */
    int getQueueCapacity();

    /**
     * 获取队队列名字
     *
     * @return String queueName
     */
    String getName();

}
