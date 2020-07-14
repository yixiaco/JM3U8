package com.hexm.util;


import cn.hutool.core.thread.ThreadFactoryBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 公共线程池服务
 *
 * @author hexm
 * @date 2018/10/10
 */
public class BusServiceThreadPool {

    private static class SysService {

        /**
         * 线程池缓存
         */
        private static final Map<String, ThreadPoolExecutor> THREAD_POOL_EXECUTOR_CACHE_MAP = new HashMap<>();

        /**
         * 得到一个线程池
         *
         * @param poolName 线程前缀
         * @param priority 优先级 1~10
         * @return
         */
        public static ThreadPoolExecutor getInstance(String poolName, int priority) {
            //此处使用双重否定同步语句，提高缓存建立与访问效率
            if (!THREAD_POOL_EXECUTOR_CACHE_MAP.containsKey(poolName)) {
                synchronized (SysService.class) {
                    if (!THREAD_POOL_EXECUTOR_CACHE_MAP.containsKey(poolName)) {
                        THREAD_POOL_EXECUTOR_CACHE_MAP.put(poolName, getThreadPoolExecutor(poolName, priority));
                    }
                }
            }
            //如果线程池已经被关闭，需要重新实例化一个线程池
            if (THREAD_POOL_EXECUTOR_CACHE_MAP.get(poolName).isShutdown()) {
                THREAD_POOL_EXECUTOR_CACHE_MAP.put(poolName, getThreadPoolExecutor(poolName, priority));
            }
            return THREAD_POOL_EXECUTOR_CACHE_MAP.get(poolName);
        }

        /**
         * 根据提供的名称，创建线程池
         *
         * @param poolName
         * @param priority 优先级 1~10
         * @return
         */
        private static ThreadPoolExecutor getThreadPoolExecutor(String poolName, int priority) {
            ThreadPoolExecutor pool;
            //获取cpu核心，并计算核心线程数
            int corePoolSize = (int) (Runtime.getRuntime().availableProcessors() * 0.8 * (1 + 1.5));
            int maximumPoolSize = corePoolSize * 5;
            //120秒空闲销毁线程
            long keepActiveTime = 120L;
            pool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepActiveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNamePrefix(poolName + "-").setPriority(priority).build(), new ThreadPoolExecutor.AbortPolicy());

            //为节省服务器资源，核心线程将在超过空闲时间时允许被销毁
            //pool.allowCoreThreadTimeOut(true);
            return pool;
        }
    }

    /**
     * 私有化构造方法，因为这不是一个正确的使用方式
     */
    private BusServiceThreadPool() {
    }

    /**
     * 获取线程池实例，如果线程池已经存在，则直接从缓存中获取，如果不存在，则创建一个新的线程池实例
     *
     * @param poolName 线程前缀
     * @param priority 优先级 1~10
     * @return
     */
    public static ThreadPoolExecutor getInstance(String poolName, int priority) {
        return SysService.getInstance(poolName, priority);
    }

    /**
     * 停止全部线程池
     */
    public static void shutdownAll() {
        SysService.THREAD_POOL_EXECUTOR_CACHE_MAP.forEach((key, val) -> {
            val.shutdown();
        });
    }

    /**
     * 立即停止全部线程池，无论是否执行完成
     */
    public static void shutdownNowAll() {
        SysService.THREAD_POOL_EXECUTOR_CACHE_MAP.forEach((key, val) -> {
            val.shutdownNow();
        });
    }
}
