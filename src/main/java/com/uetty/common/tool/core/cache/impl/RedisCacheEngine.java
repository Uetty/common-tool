package com.uetty.common.tool.core.cache.impl;

import com.uetty.common.tool.core.cache.CacheEngine;
import com.uetty.common.tool.core.cache.CacheManager;
import com.uetty.common.tool.core.cache.mo.Lock;
import com.uetty.common.tool.core.spring.SpringContextPeeper;
import com.uetty.common.tool.core.string.StringUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis缓存引擎
 */
public class RedisCacheEngine implements CacheEngine {

    @Override
    public String cacheType() {
        return CacheManager.CACHE_TYPE_REDIS;
    }

    private volatile RedisTemplate<String, Object> redisTemplate;

    private static final String COMPARE_AND_DEL_LUA =
            "local str = redis.call('get', KEYS[1]);\n" +
                    "if (str == nil) then\n" +
                    "    return 0;\n" +
                    "else\n" +
                    "    if (str == ARGV[1]) then\n" +
                    "        redis.call('del', KEYS[1]);\n" +
                    "        return 1;\n" +
                    "    end\n" +
                    "end\n" +
                    "return 0;";

    private static volatile RedisScript<Long> COMPARE_AND_DEL_SCRIPT;

    public static RedisScript<Long> getCompareAndDelScript() {
        if (COMPARE_AND_DEL_SCRIPT != null) {
            return COMPARE_AND_DEL_SCRIPT;
        }
        synchronized (COMPARE_AND_DEL_LUA) {
            if (COMPARE_AND_DEL_SCRIPT != null) {
                return COMPARE_AND_DEL_SCRIPT;
            }
            COMPARE_AND_DEL_SCRIPT = new DefaultRedisScript<>(COMPARE_AND_DEL_LUA, Long.class);
        }
        return COMPARE_AND_DEL_SCRIPT;
    }

    private static final String DEFAULT_LOCK_PREFIX = "lock:";
    private static final String UNLOCK_LUA =
            "local str = redis.call('get', KEYS[1]);\n" +
                    "if (str == nil) then\n" +
                    "    return 0;\n" +
                    "else\n" +
                    "    if (str == ARGV[1]) then\n" +
                    "        redis.call('del', KEYS[1]);\n" +
                    "        return 1;\n" +
                    "    end\n" +
                    "end\n" +
                    "return 0;";

    private static RedisScript<Long> UNLOCK_SCRIPT;

    public static RedisScript<Long> getUnlockScript() {
        if (UNLOCK_SCRIPT != null) {
            return UNLOCK_SCRIPT;
        }
        synchronized (UNLOCK_LUA) {
            if (UNLOCK_SCRIPT != null) {
                return UNLOCK_SCRIPT;
            }
            UNLOCK_SCRIPT = new DefaultRedisScript<>(UNLOCK_LUA, Long.class);
        }
        return UNLOCK_SCRIPT;
    }

    /**
     * 默认每个缓存生效时间30分钟
     */
    public static final long CACHE_HOLD_TIME_30MIN = 30 * 60 * 1000L;

    public static final String CACHE_TYPE_FASTJSON = "app:cacheEngine:%s";

    private RedisTemplate<String, Object> getRedisTemplate() {
        if (redisTemplate != null) {
            return redisTemplate;
        }
        redisTemplate = (RedisTemplate<String, Object>) SpringContextPeeper.getBeanQuiet(RedisTemplate.class, "redisTemplate");
        if (redisTemplate == null) {
            throw new RuntimeException("redis template not found");
        }
        return redisTemplate;
    }

    @Override
    public <T> void put(String cacheName, T obj, Long expirationTime) {
        if (obj == null) {
            return;
        }

        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

        if (expirationTime == null) {
            expirationTime = CACHE_HOLD_TIME_30MIN;
        }

        String key = String.format(CACHE_TYPE_FASTJSON, cacheName);

        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        opsForValue.set(key, obj, Duration.ofMillis(expirationTime));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String cacheName) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

        String key = String.format(CACHE_TYPE_FASTJSON, cacheName);

        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        Object o = opsForValue.get(key);
        if (o == null) {
            return null;
        }

        return (T) o;
    }

    @Override
    public void removeAll() {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

        String key = String.format(CACHE_TYPE_FASTJSON, "*");

        Set<String> keys = redisTemplate.keys(key);
        if (keys == null || keys.size() == 0) {
            return;
        }
        redisTemplate.delete(keys);
    }

    @Override
    public void remove(String cacheName) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        String key = String.format(CACHE_TYPE_FASTJSON, cacheName);
        redisTemplate.delete(key);
    }

    @Override
    public <T> void remove(String cacheName, T data) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        String key = String.format(CACHE_TYPE_FASTJSON, cacheName);

        List<String> keys = new ArrayList<>();
        keys.add(key);
        // 用于校验持有锁的人是否是自己
        Object[] args = new Object[] {data};
        try {
            // 释放锁
            redisTemplate.execute(getCompareAndDelScript(), keys, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateExpiration(String cacheName, long expirationMillis) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        String key = String.format(CACHE_TYPE_FASTJSON, cacheName);
        redisTemplate.expire(key, Duration.ofMillis(expirationMillis));
    }

    @Override
    public boolean checkExists(String cacheName) {

        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

        String key = String.format(CACHE_TYPE_FASTJSON, cacheName);

        Boolean result = redisTemplate.hasKey(key);

        return Boolean.TRUE.equals(result);
    }

    @Override
    public List<String> scanCachePrefix(String cachePrefix) {

        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

        String basePrefix = String.format(CACHE_TYPE_FASTJSON, "");
        String keyPattern = basePrefix + cachePrefix + "*";
        Set<String> keys = redisTemplate.keys(keyPattern);

        List<String> list = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                if (!key.startsWith(basePrefix)) {
                    continue;
                }
                list.add(key.substring(basePrefix.length()));
            }
        }
        return list;
    }

    @Override
    public long getExpireSeconds(String cacheName) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();

        String key = String.format(CACHE_TYPE_FASTJSON, cacheName);

        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (expire == null) {
            return -1;
        }
        return expire;
    }

    /**
     * 获取锁
     * @param key 锁名
     * @param autoReleaseSeconds 自动释放的秒数（防止宕机未释放）
     * @return 锁对象（如果未获取到锁，返回null）
     */
    @Override
    public Lock lock(String key, int autoReleaseSeconds) {
        RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
        ValueOperations<String, Object> opsForValue = redisTemplate.opsForValue();
        try {
            Lock lock = new RedisCacheLock();
            lock.setKey(DEFAULT_LOCK_PREFIX + key);
            lock.setToken(UUID.randomUUID().toString());

            Boolean aBoolean = opsForValue.setIfAbsent(lock.getKey(), lock.getToken(), Duration.ofSeconds(autoReleaseSeconds));
            if (Objects.equals(aBoolean, true)) {
                return lock;
            }
        } catch (Exception ignore) {
        }
        return null;
    }

    /**
     * 获取锁(2分钟后自动释放锁，防止宕机情况未释放锁)
     * @param key 锁名
     * @return 锁对象（如果未获取到锁，返回null）
     */
    @Override
    public Lock lock(String key) {
        return lock(key, 120);
    }

    public class RedisCacheLock extends Lock {
        /**
         * 释放锁
         */
        public void release() {
            List<String> keys = new ArrayList<>();
            keys.add(getKey());
            // 用于校验持有锁的人是否是自己
            Object[] args = new Object[] {getToken()};
            try {
                RedisScript<Long> unlockScript = getUnlockScript();
                RedisTemplate<String, Object> redisTemplate = getRedisTemplate();
                // 释放锁
                redisTemplate.execute(unlockScript, keys, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
