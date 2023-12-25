package com.uetty.common.tool.core.cloud.huaweiyun.token.delegation;

import com.alibaba.fastjson2.JSON;
import com.uetty.common.tool.core.DateUtil;
import com.uetty.common.tool.core.cache.CacheEngine;
import com.uetty.common.tool.core.cache.impl.MemoryCacheEngine;
import com.uetty.common.tool.core.cache.impl.RedisCacheEngine;
import com.uetty.common.tool.core.cloud.huaweiyun.token.TokenProvider;
import com.uetty.common.tool.core.cloud.huaweiyun.token.delegation.mo.HuaweiyunDelegationConfig;
import com.uetty.common.tool.core.cloud.huaweiyun.token.delegation.mo.HuaweiyunDelegationRespVo;
import com.uetty.common.tool.core.cloud.huaweiyun.token.mo.HuaweiyunAccessToken;
import com.uetty.common.tool.core.cloud.huaweiyun.token.mo.HuaweiyunAccessTokenVo;
import com.uetty.common.tool.core.json.fastjson2.FastJsonUtil;
import com.uetty.common.tool.core.net.HttpClientUtil;
import com.uetty.common.tool.core.spring.SpringContextPeeper;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

/**
 * 华为云Delegation方式获取token
 * @author vince
 */
@Slf4j
public class HuaweiyunDelegationTokenProvider implements TokenProvider<HuaweiyunDelegationConfig> {

    private volatile CacheEngine cacheEngine;

    private static final String TOKEN_KEY = "delegation_token:%s";

    private static volatile HuaweiyunDelegationTokenProvider SINGLETON;
    /**
     * 不缓存在过期前20分钟提前丢弃token
     */
    private static final int CACHE_DISCARD_BEFORE_EXPIRE_SECONDS = 20 * 60;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4, 4, SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(DAY_OF_MONTH, 2)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(":")
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(":")
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral(".")
            .appendValue(ChronoField.MICRO_OF_SECOND, 3, 9, SignStyle.NEVER)
            // parse时：根据时间字符串中声明的时区转换为时间戳
            // format时：输出时区信息
            // 参数2声明0时区时使用"Z"
            .appendOffset("+HHMM", "Z")
            .toFormatter()
            // parse时：如果时间字符串中有时区信息，以时间字符串时区为准
            // parse时：如果时间字符串中没有时区信息，则这个时区字段必须设置并以此时区为准
            // format时，以这个时区为标准转换输出时间和时区信息
            .withZone(ZoneId.of("Asia/Shanghai"))
            ;

    protected SimpleDateFormat getTimeFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat;
    }

    private HuaweiyunDelegationTokenProvider() {
    }

    public static HuaweiyunDelegationTokenProvider getInstance() {
        if (SINGLETON != null) {
            return SINGLETON;
        }
        synchronized (HuaweiyunDelegationTokenProvider.class) {
            if (SINGLETON != null) {
                return SINGLETON;
            }
            SINGLETON = new HuaweiyunDelegationTokenProvider();
        }
        return SINGLETON;
    }

    private String buildCacheKey(HuaweiyunDelegationConfig config) {
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
            log.debug("delegation use redis cache engine");
        }
        if (this.cacheEngine instanceof MemoryCacheEngine) {
            log.debug("delegation use memory cache engine");
        }
    }

    private CacheEngine getCacheEngine() {
        if (this.cacheEngine != null) {
            return this.cacheEngine;
        }
        this.cacheEngine = SpringContextPeeper.getBeanQuiet(MemoryCacheEngine.class);
        logCacheType();
        return this.cacheEngine;
    }

    private HuaweiyunAccessToken getTokenFromCache(HuaweiyunDelegationConfig config) {
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

    private void putCache(HuaweiyunDelegationConfig config, HuaweiyunAccessToken accessToken) {
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
    public HuaweiyunAccessToken getAccessToken(HuaweiyunDelegationConfig config) {

        HuaweiyunAccessToken accessToken = getTokenFromCache(config);
        if (accessToken != null) {
            log.debug("access token[expire={},key={},token={}] get from cache", accessToken.getExpiryDate(), accessToken.getAccessKeyId(), accessToken.getToken());
            return accessToken;
        }

        accessToken = acquireToken(config);

        log.debug("acquired delegation token[expire={},key={},token={}]", accessToken.getExpiryDate(), accessToken.getAccessKeyId(), accessToken.getToken());
        putCache(config, accessToken);

        return accessToken;
    }

    public HuaweiyunAccessToken acquireToken(HuaweiyunDelegationConfig config) {

        String tokenUrl = config.getTokenUrl();

        Map<String, Object> headers = new HashMap<>();
        Map<String, Object> params = new HashMap<>();

        HttpClientUtil.HttpResponseVo httpResponseVo = HttpClientUtil.doGet(tokenUrl, headers, params);
        if (httpResponseVo.getCode() == null || httpResponseVo.getCode() != 200) {
            throw new RuntimeException("failed get access token");
        }
        String body = httpResponseVo.getBody();
        if (body == null || "".equals(body.trim())) {
            throw new RuntimeException("failed get access token");
        }

        HuaweiyunDelegationRespVo respVo = FastJsonUtil.jsonToObject(body, HuaweiyunDelegationRespVo.class);

        String expiration = respVo.getExpiresAt();
        log.debug("expiration of delegation token from huaweiyun raw value: {}", expiration);

        HuaweiyunAccessToken token = new HuaweiyunAccessToken();
        token.setAccessKeyId(respVo.getAccess());
        token.setAccessKeySecret(respVo.getSecret());
        token.setToken(respVo.getSecuritytoken());

        TemporalAccessor parse = DATE_TIME_FORMATTER.parse(expiration);
        long epochMilli = Instant.from(parse).toEpochMilli();
        token.setExpiryDate(new Date(epochMilli));

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

    public List<HuaweiyunAccessTokenVo> getCachedTokenInfo() {

        String keyPrefix = String.format(TOKEN_KEY, "");

        CacheEngine cacheEngine = getCacheEngine();
        if (cacheEngine == null) {
            return new ArrayList<>();
        }

        List<String> keys = cacheEngine.scanCachePrefix(keyPrefix);
        String cacheLocation = cacheEngine instanceof RedisCacheEngine ? "redis" : "local";

        List<HuaweiyunAccessTokenVo> list = new ArrayList<>();
        for (String key : keys) {
            HuaweiyunAccessToken accessToken = cacheEngine.get(key);
            if (accessToken == null) {
                continue;
            }
            long expireSeconds = cacheEngine.getExpireSeconds(key);

            HuaweiyunAccessTokenVo accessTokenVo = new HuaweiyunAccessTokenVo();
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


    public static void main(String[] args) throws ParseException {

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendValue(YEAR, 4, 4, SignStyle.EXCEEDS_PAD)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 2)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral('T')
                .appendValue(ChronoField.HOUR_OF_DAY, 2)
                .appendLiteral(":")
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendLiteral(":")
                .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                .appendLiteral(".")
                .appendValue(ChronoField.MICRO_OF_SECOND, 3, 9, SignStyle.NEVER)
                // parse时：根据时间字符串中声明的时区转换为时间戳
                // format时：输出时区信息
                .appendOffset("+HHMM", "Z")  // 参数2声明0时区时使用"Z"
                .toFormatter()
                // parse时：如果时间字符串中有时区信息，以时间字符串时区为准
                // parse时：如果时间字符串中没有时区信息，则这个时区字段必须设置并以此时区为准
                // format时，以这个时区为标准转换输出时间和时区信息
                .withZone(ZoneId.of("Asia/Shanghai"))
                ;
        // 打印结果：2023-11-11T20:58:43.429000Asia/Shanghai
        System.out.println(formatter.format(OffsetDateTime.now()));

        TemporalAccessor parse = formatter.parse("2023-11-11T21:04:17.409000Asia/Shanghai");
        System.out.println(Instant.from(parse));
    }
}
