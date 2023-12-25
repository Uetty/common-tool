package com.uetty.common.tool.core.cloud.aliyun.token.delegation;

import com.alibaba.fastjson2.JSON;
import com.uetty.common.tool.core.DateUtil;
import com.uetty.common.tool.core.cache.CacheEngine;
import com.uetty.common.tool.core.cache.impl.MemoryCacheEngine;
import com.uetty.common.tool.core.cache.impl.RedisCacheEngine;
import com.uetty.common.tool.core.cloud.aliyun.token.TokenProvider;
import com.uetty.common.tool.core.cloud.aliyun.token.delegation.mo.AliyunEcsRamConfig;
import com.uetty.common.tool.core.cloud.aliyun.token.delegation.mo.AliyunEcsRamRespVo;
import com.uetty.common.tool.core.cloud.aliyun.token.mo.AliyunAccessToken;
import com.uetty.common.tool.core.cloud.aliyun.token.mo.AliyunAccessTokenVo;
import com.uetty.common.tool.core.json.fastjson2.FastJsonUtil;
import com.uetty.common.tool.core.net.HttpClientUtil;
import com.uetty.common.tool.core.spring.SpringContextPeeper;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * 阿里云ECS RAM 方式获取token
 * @author vince
 */
@Slf4j
public class AliyunEcsRamTokenProvider implements TokenProvider<AliyunEcsRamConfig> {

    private volatile CacheEngine cacheEngine;

    private static final String TOKEN_KEY = "ecs_ram_token:%s";

    private static volatile AliyunEcsRamTokenProvider SINGLETON;
    /**
     * 不缓存在过期前20分钟提前丢弃token
     */
    private static final int CACHE_DISCARD_BEFORE_EXPIRE_SECONDS = 20 * 60;

//    protected SimpleDateFormat getTimeFormat() {
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.XXX");
//        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//        return simpleDateFormat;
//    }

    private AliyunEcsRamTokenProvider() {
    }

    public static AliyunEcsRamTokenProvider getInstance() {
        if (SINGLETON != null) {
            return SINGLETON;
        }
        synchronized (AliyunEcsRamTokenProvider.class) {
            if (SINGLETON != null) {
                return SINGLETON;
            }
            SINGLETON = new AliyunEcsRamTokenProvider();
        }
        return SINGLETON;
    }

    private String buildCacheKey(AliyunEcsRamConfig config) {
        byte[] jsonBytes = JSON.toJSONBytes(config);
        Base64.Encoder encoder = Base64.getEncoder();
        String base64 = encoder.encodeToString(jsonBytes);
        return String.format(TOKEN_KEY, base64);
    }

    private void logCacheType() {
        if (this.cacheEngine == null) {
            return;
        }
        if (this.cacheEngine instanceof RedisCacheEngine) {
            log.debug("ecs ram use redis cache engine");
        }
        if (this.cacheEngine instanceof MemoryCacheEngine) {
            log.debug("ecs ram use memory cache engine");
        }
    }

    private CacheEngine getCacheEngine() {
        if (this.cacheEngine != null) {
            return this.cacheEngine;
        }
        // 元数据加固过的ecs ram token 是只针对单台机器有效的，就不用redis做缓存了
        this.cacheEngine = SpringContextPeeper.getBeanQuiet(MemoryCacheEngine.class);
        logCacheType();
        return this.cacheEngine;
    }

    private AliyunAccessToken getTokenFromCache(AliyunEcsRamConfig config) {
        CacheEngine cacheEngine = getCacheEngine();
        if (cacheEngine == null) {
            return null;
        }

        String cacheKey = buildCacheKey(config);

        try {
            return cacheEngine.get(cacheKey);
        } catch (Exception e) {
            log.warn("get from cache error, {}", e.getMessage());
            return null;
        }
    }

    private void putCache(AliyunEcsRamConfig config, AliyunAccessToken accessToken) {
        CacheEngine cacheEngine = getCacheEngine();
        if (cacheEngine == null) {
            log.warn("no cache engine");
            return;
        }

        Date expiryDate = accessToken.getExpiryDate();
        // 距离过期的秒数
        long durationSeconds = DateUtil.getDurationSeconds(new Date(), expiryDate);
        // 将距离过期的秒数减掉20分钟，提前丢弃token，防止时间临界点失效
        durationSeconds = durationSeconds - CACHE_DISCARD_BEFORE_EXPIRE_SECONDS;
        log.debug("durationSeconds is {}", durationSeconds);
        if (durationSeconds <= 0) {
            // 过期时间小于20分钟，不缓存
            return;
        }

        // 处理异常时间，非常大的时间值
        int maxExpireSeconds = Math.max(config.getMaxDurationSeconds() - CACHE_DISCARD_BEFORE_EXPIRE_SECONDS, 60);
        if (durationSeconds > maxExpireSeconds) {
            durationSeconds = maxExpireSeconds;
        }
        String cacheKey = buildCacheKey(config);

        try {
            long expiryTimestamp = System.currentTimeMillis() + durationSeconds * 1000;
            cacheEngine.put(cacheKey, accessToken, expiryTimestamp);
        } catch (Exception e) {
            log.warn("cache operation failed, {}", e.getMessage());
        }
    }

    @Override
    public AliyunAccessToken getAccessToken(AliyunEcsRamConfig config) {

        AliyunAccessToken accessToken = getTokenFromCache(config);
        if (accessToken != null) {
            log.debug("access token[expire={},key={},token={}] get from cache", accessToken.getExpiryDate(), accessToken.getAccessKeyId(), accessToken.getToken());
            return accessToken;
        }

        accessToken = acquireToken(config);

        log.debug("acquired ecs ram token[expire={},key={},token={}]", accessToken.getExpiryDate(), accessToken.getAccessKeyId(), accessToken.getToken());
        putCache(config, accessToken);

        return accessToken;
    }

    public AliyunAccessToken acquireToken(AliyunEcsRamConfig config) {

        boolean metaReinforce = config.isMetaReinforce();
        String metadata = "";
        if (metaReinforce) {
            // 元数据加固过的机器，能防止SSRF，必须请求这个url获取元数据
            String metaUrl = config.getMetaUrl();
            String metaUrlHeaderTtl = config.getMetaUrlHeaderTtl();
            int defaultTtl = config.getDefaultTtl();

            Map<String, Object> headers = new HashMap<>();
            headers.put(metaUrlHeaderTtl, defaultTtl);
            Map<String, Object> params = new HashMap<>();
            HttpClientUtil.HttpResponseVo httpResponseVo = HttpClientUtil.doPut(metaUrl, headers, params);
            if (httpResponseVo.getCode() == null || httpResponseVo.getCode() != 200) {
                throw new RuntimeException("failed get access token");
            }
            metadata = httpResponseVo.getBody();
        }

        String tokenUrlTemplate = config.getTokenUrl();
        String tokenUrlHeaderMeta = config.getTokenUrlHeaderMeta();

        Map<String, Object> headers = new HashMap<>();
        if (metaReinforce) {
            headers.put(tokenUrlHeaderMeta, metadata);
        }
        Map<String, Object> params = new HashMap<>();
        String tokenUrl = String.format(tokenUrlTemplate, config.getRamRoleName());
        HttpClientUtil.HttpResponseVo httpResponseVo = HttpClientUtil.doGet(tokenUrl, headers, params);
        if (httpResponseVo.getCode() == null || httpResponseVo.getCode() != 200) {
            throw new RuntimeException("failed get access token");
        }
        String body = httpResponseVo.getBody();
        if (body == null || "".equals(body.trim())) {
            throw new RuntimeException("failed get access token");
        }

        AliyunEcsRamRespVo respVo = FastJsonUtil.jsonToObject(body, AliyunEcsRamRespVo.class);

        String expiration = respVo.getExpiration();
        log.debug("expiration of ecs ram token from aliyun raw value: {}", expiration);

        AliyunAccessToken token = new AliyunAccessToken();
        token.setAccessKeyId(respVo.getAccessKeyId());
        token.setAccessKeySecret(respVo.getAccessKeySecret());
        token.setToken(respVo.getSecurityToken());

        // Instant可以处理这种字符串并且兼容XXX从3位到9位
        // SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.XXX");
        Instant instant = Instant.parse(expiration);
        Date parse = new Date(instant.toEpochMilli());
        token.setExpiryDate(parse);

        return token;
    }

    private String fuzzy(String str, int head, int tail) {
        if (str == null || str.length() < head + tail) {
            return str;
        }

        char[] charArray = str.toCharArray();
        for (int i = head; i < charArray.length - tail; i++) {
            charArray[i] = '*';
        }
        return new String(charArray);
    }

    public List<AliyunAccessTokenVo> getCachedTokenInfo() {

        String keyPrefix = String.format(TOKEN_KEY, "");

        CacheEngine cacheEngine = getCacheEngine();
        if (cacheEngine == null) {
            return new ArrayList<>();
        }

        List<String> keys = cacheEngine.scanCachePrefix(keyPrefix);
        String cacheLocation = cacheEngine instanceof RedisCacheEngine ? "redis" : "local";

        List<AliyunAccessTokenVo> list = new ArrayList<>();
        for (String key : keys) {
            AliyunAccessToken accessToken = cacheEngine.get(key);
            if (accessToken == null) {
                continue;
            }
            long expireSeconds = cacheEngine.getExpireSeconds(key);

            AliyunAccessTokenVo accessTokenVo = new AliyunAccessTokenVo();
            String accessKeyId = accessToken.getAccessKeyId();
            accessTokenVo.setAccessKeyId(fuzzy(accessKeyId, 10, 6));
            accessTokenVo.setToken(fuzzy(accessToken.getToken(), 10, 6));
            accessTokenVo.setCacheLocation(cacheLocation);
            accessTokenVo.setExpiryDate(accessToken.getExpiryDate());

            accessTokenVo.setExpireSeconds((int) expireSeconds);

            list.add(accessTokenVo);
        }

        return list;
    }

    public void clearTokenCache() {
        CacheEngine cacheEngine = getCacheEngine();
        if (cacheEngine == null) {
            return;
        }
        String keyPrefix = String.format(TOKEN_KEY, "");
        List<String> keys = cacheEngine.scanCachePrefix(keyPrefix);
        if (keys != null) {
            for (String key : keys) {
                cacheEngine.remove(key);
            }
        }
    }

    public static void main(String[] args) {
        AliyunEcsRamTokenProvider aliyunEcsRamTokenProvider = AliyunEcsRamTokenProvider.getInstance();

        AliyunEcsRamConfig aliyunEcsRamConfig = new AliyunEcsRamConfig();
        aliyunEcsRamConfig.setRamRoleName("xxx");

        AliyunAccessToken accessToken = aliyunEcsRamTokenProvider.acquireToken(aliyunEcsRamConfig);

        System.out.println(accessToken);
    }
}
