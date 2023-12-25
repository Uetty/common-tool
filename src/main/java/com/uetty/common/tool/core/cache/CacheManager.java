package com.uetty.common.tool.core.cache;

import com.alibaba.fastjson2.JSONObject;
import com.uetty.common.tool.core.spring.SpringContextPeeper;
import com.uetty.common.tool.core.string.StringUtil;

import java.util.List;

public class CacheManager {

    public static final String CACHE_TYPE_MEMORY = "memory";
    public static final String CACHE_TYPE_REDIS = "redis";

    private static volatile CacheEngine cacheEngine;

    private static void initCacheEngine() {
        if (cacheEngine != null) {
            return;
        }
        synchronized (CacheManager.class) {
            if (cacheEngine != null) {
                return;
            }
            cacheEngine = SpringContextPeeper.getBeanQuiet(CacheEngine.class);
        }

        if (cacheEngine == null) {
            throw new RuntimeException("cache engine not initialized");
        }
    }

    private static CacheEngine getCacheEngine(String cacheType) {

        List<CacheEngine> cacheEngineList = SpringContextPeeper.getBeansQuiet(CacheEngine.class);
        for (CacheEngine engine : cacheEngineList) {
            if (StringUtil.equals(engine.cacheType(), cacheType)) {
                return engine;
            }
        }

        return null;
    }

    /**
     * 存放一个缓存对象
     *
     * @param cacheName      key
     * @param obj            object
     * @param expirationTime 缓存时间，如果过期时间没有被定义，则默认30分钟，否则按照定义时间
     */
    public static <T> void put(String cacheType, String cacheName, T obj, Long expirationTime) {
        CacheEngine cacheEngine = getCacheEngine(cacheType);
        if (cacheEngine == null) {
            return;
        }
        cacheEngine.put(cacheName, obj, expirationTime);
    }

    /**
     * 取出一个缓存对象
     */
    public static <T> T get(String cacheType, String cacheName) {
        CacheEngine cacheEngine = getCacheEngine(cacheType);
        if (cacheEngine == null) {
            return null;
        }
        return cacheEngine.get(cacheName);
    }

    /**
     * 存放一个缓存对象
     *
     * @param cacheName      key
     * @param obj            object
     * @param expirationTime 缓存时间，如果过期时间没有被定义，则默认30分钟，否则按照定义时间
     */
    public static <T> void put(String cacheName, T obj, Long expirationTime) {
        initCacheEngine();

        cacheEngine.put(cacheName, obj, expirationTime);
    }

    /**
     * 取出一个缓存对象
     *
     * @param cacheName
     * @return
     */
    public static <T> T get(String cacheName) {
        initCacheEngine();
        return cacheEngine.get(cacheName);
    }

    public static void removeAll() {
        initCacheEngine();
        cacheEngine.removeAll();
    }

    /**
     * 删除某个缓存
     *
     * @param cacheName
     */
    public static void remove(String cacheName) {
        initCacheEngine();
        cacheEngine.remove(cacheName);
    }

    public static <T> void remove(String cacheType, String cacheName, T data) {
        CacheEngine cacheEngine = getCacheEngine(cacheType);
        if (cacheEngine == null) {
            return;
        }
        cacheEngine.remove(cacheName, data);
    }

    /**
     * 检查缓存对象是否存在，
     * 若不存在，则返回false
     * 若存在，检查其是否已过有效期，如果已经过了则删除该缓存并返回false
     *
     * @param cacheName
     * @return
     */
    public static boolean checkExists(String cacheName) {
        initCacheEngine();
        return cacheEngine.checkExists(cacheName);
    }
}
