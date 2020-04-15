package com.tss.nettysocket.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.*;

/**
 * ${description}
 *
 * @author wangyaoqin
 * @date 2020/3/14
 */
public class ThreadPoolFactory {
    private static final Logger log = LoggerFactory.getLogger(ThreadPoolFactory.class);

    /** 核心线程池默认大小 */
    private static final Integer DEFAULT_CORE_POOL_SIZE = 8;
    /** 默认最大线程数 */
    private static final Integer DEFAULT_MAX_POOL_SIZE = 64;
    /** 默认空闲线程存活时间(s) */
    private static final Integer DEFAULT_FREE_THREAD_KEEP_ALIVE = 60;
    /** 默认任务队列大小 */
    private static final Integer DEFAULT_TASK_QUEUE_SIZE = 2000;
    /** 默认的线程池饱和时任务拒绝策略为打印日志(包含线程池的线程数配置，队列大小配置，线程池分组名称) */
    private static final RejectedExecutionHandler DEFAULT_REJECTED_HANDLER = new LogPolicy();

    /** 线程池对象 */
    private ThreadPoolExecutor executor;

    /** 工厂实例 */
    private static volatile ThreadPoolFactory instance;


    private ThreadPoolFactory(Integer coolPoolSize, Integer maxPoolSize, Integer keepAliveTime, TimeUnit timeUnit,
                              Integer taskQueueSize, RejectedExecutionHandler handler) {
        this.executor = new ThreadPoolExecutor(coolPoolSize, maxPoolSize, keepAliveTime, timeUnit,
                new LinkedBlockingQueue<>(taskQueueSize), handler);
    }

    /**
     * 线程安全创建线程池对象
     * @return ThreadPoolFactory
     */
    public static ThreadPoolFactory getInstance() {
        if (null == instance) {
            synchronized (ThreadPoolFactory.class) {
                if (null == instance) {
                    instance =  new ThreadPoolFactory(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAX_POOL_SIZE, DEFAULT_FREE_THREAD_KEEP_ALIVE,
                            TimeUnit.SECONDS, DEFAULT_TASK_QUEUE_SIZE, DEFAULT_REJECTED_HANDLER);
                }
            }
        }
        return instance;
    }

    /**
     * 获取executor对象
     * @return
     */
    public ThreadPoolExecutor executor(){
        return this.executor;
    }

    /**
     * 线程池饱和时任务拒绝策略
     */
    public static class LogPolicy implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            ThreadFactory factory = executor.getThreadFactory();
            String threadNamePrefix = "";
            if (factory.getClass() == CustomizableThreadFactory.class) {
                CustomizableThreadFactory factory1 = (CustomizableThreadFactory) factory;
                threadNamePrefix = factory1.getThreadNamePrefix();
            }
            log.error("threadNamePrefix {}, corePoolSize {}, maxPoolSize {}, workQueueSize {}, rejected task {}.",
                    new Object[]{threadNamePrefix, executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getQueue().size(), r.toString()});
        }
    }
}
