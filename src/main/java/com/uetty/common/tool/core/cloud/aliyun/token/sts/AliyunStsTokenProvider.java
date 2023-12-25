package com.uetty.common.tool.core.cloud.aliyun.token.sts;

import com.alibaba.fastjson2.JSON;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.auth.sts.AssumeRoleRequest;
import com.aliyuncs.auth.sts.AssumeRoleResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.uetty.common.tool.core.DateUtil;
import com.uetty.common.tool.core.cache.CacheEngine;
import com.uetty.common.tool.core.cache.impl.MemoryCacheEngine;
import com.uetty.common.tool.core.cache.impl.RedisCacheEngine;
import com.uetty.common.tool.core.cloud.aliyun.token.TokenProvider;
import com.uetty.common.tool.core.cloud.aliyun.token.sts.mo.AliyunStsKey;
import com.uetty.common.tool.core.cloud.aliyun.token.sts.mo.AliyunStsToken;
import com.uetty.common.tool.core.cloud.aliyun.token.sts.mo.StsInfoVo;
import com.uetty.common.tool.core.spring.SpringContextPeeper;
import com.uetty.common.tool.core.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * 阿里云STS token管理
 * @author vince
 */
@Slf4j
public class AliyunStsTokenProvider implements TokenProvider<AliyunStsKey> {

    private volatile CacheEngine cacheEngine;

    private boolean preferRedisCacheEngine = true;

    private static final long DEFAULT_DURATION_SECONDS = 60 * 60;

    private static final String STS_KEY = "sts:%s";

    private static volatile AliyunStsTokenProvider SINGLETON;
    /**
     * 不缓存在过期前20分钟提前丢弃token
     */
    private static final int CACHE_DISCARD_BEFORE_EXPIRE_SECONDS = 20 * 60;

    protected SimpleDateFormat getTimeFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat;
    }

    private AliyunStsTokenProvider() {
    }

    public static AliyunStsTokenProvider getInstance() {
        if (SINGLETON != null) {
            return SINGLETON;
        }
        synchronized (AliyunStsTokenProvider.class) {
            if (SINGLETON != null) {
                return SINGLETON;
            }
            SINGLETON = new AliyunStsTokenProvider();
        }
        return SINGLETON;
    }

    public boolean isPreferRedisCacheEngine() {
        return preferRedisCacheEngine;
    }

    public void setPreferRedisCacheEngine(boolean preferRedisCacheEngine) {
        this.preferRedisCacheEngine = preferRedisCacheEngine;
    }

    private String buildCacheKey(AliyunStsKey key) {
        byte[] jsonBytes = JSON.toJSONBytes(key);
        Base64.Encoder encoder = Base64.getEncoder();
        String base64 = encoder.encodeToString(jsonBytes);
        return String.format(STS_KEY, base64);
    }

    private void logCacheType() {
        if (this.cacheEngine == null) {
            return;
        }
        if (this.cacheEngine instanceof RedisCacheEngine) {
            log.debug("use redis cache engine");
        }
        if (this.cacheEngine instanceof MemoryCacheEngine) {
            log.debug("use memory cache engine");
        }
    }

    private CacheEngine getCacheEngine() {
        if (preferRedisCacheEngine && !(this.cacheEngine instanceof RedisCacheEngine)) {
            this.cacheEngine = SpringContextPeeper.getBeanQuiet(RedisCacheEngine.class);
            logCacheType();
        }
        if (this.cacheEngine != null) {
            return this.cacheEngine;
        }
        this.cacheEngine = SpringContextPeeper.getBeanQuiet(RedisCacheEngine.class);
        logCacheType();
        if (this.cacheEngine == null) {
            this.cacheEngine = SpringContextPeeper.getBeanQuiet(MemoryCacheEngine.class);
            logCacheType();
        }
        return this.cacheEngine;
    }

    private AliyunStsToken getStsTokenFromCache(AliyunStsKey key) {
        CacheEngine cacheEngine = getCacheEngine();
        if (cacheEngine == null) {
            return null;
        }

        String cacheKey = buildCacheKey(key);

        try {
            return cacheEngine.get(cacheKey);
        } catch (Exception e) {
            log.warn("get from cache error, {}", e.getMessage());
            return null;
        }
    }

    private void putCache(AliyunStsKey key, AliyunStsToken stsToken) {
        CacheEngine cacheEngine = getCacheEngine();
        if (cacheEngine == null) {
            log.warn("no cache engine");
            return;
        }

        Date expiryDate = stsToken.getExpiryDate();
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
        int maxExpireSeconds = Math.max(key.getMaxDurationSeconds() - CACHE_DISCARD_BEFORE_EXPIRE_SECONDS, 60);
        if (durationSeconds > maxExpireSeconds) {
            durationSeconds = maxExpireSeconds;
        }
        String cacheKey = buildCacheKey(key);

        try {
            long expiryTimestamp = System.currentTimeMillis() + durationSeconds * 1000;
            cacheEngine.put(cacheKey, stsToken, expiryTimestamp);
        } catch (Exception e) {
            log.warn("cache operation failed, {}", e.getMessage());
        }
    }

    @Override
    public AliyunStsToken getAccessToken(AliyunStsKey key) {

        AliyunStsToken stsToken = getStsTokenFromCache(key);
        if (stsToken != null) {
            log.debug("sts token[sessionName={},roleArn={},expire={},key={},token={}] get from cache", stsToken.getRoleSessionName(), stsToken.getRoleArn(), stsToken.getExpiryDate(), stsToken.getAccessKeyId(), stsToken.getToken());
            return stsToken;
        }

        stsToken = acquireToken(key);

        log.debug("acquired sts token[sessionName={},roleArn={},expire={},key={},token={}]", stsToken.getRoleSessionName(), stsToken.getRoleArn(), stsToken.getExpiryDate(), stsToken.getAccessKeyId(), stsToken.getToken());
        putCache(key, stsToken);

        return stsToken;
    }

    public AliyunStsToken acquireToken(AliyunStsKey key) {

        DefaultProfile.addEndpoint(key.getRegionId(), "Sts", key.getEndpoint());

        // 构造default profile。
        IClientProfile profile = DefaultProfile.getProfile(key.getRegionId(), key.getAccessKeyId(), key.getAccessKeySecret());
        // 构造client。
        DefaultAcsClient client = new DefaultAcsClient(profile);
        final AssumeRoleRequest request = new AssumeRoleRequest();
        // 适用于Java SDK 3.12.0及以上版本。
        request.setSysMethod(MethodType.POST);
        request.setRoleArn(key.getRoleArn());
        request.setRoleSessionName(key.getRoleSessionName());
        if (StringUtil.isNotBlank(key.getPolicy())) {
            request.setPolicy(key.getPolicy());
        }
        request.setDurationSeconds(DEFAULT_DURATION_SECONDS);
        final AssumeRoleResponse response;
        try {
            response = client.getAcsResponse(request);

            AliyunStsToken stsToken = new AliyunStsToken();

            BeanUtils.copyProperties(key, stsToken);

            String expiration = response.getCredentials().getExpiration();
            log.debug("expiration of sts token from aliyun raw value: {}", expiration);
            SimpleDateFormat timeFormat = getTimeFormat();
            Date parse = timeFormat.parse(expiration);

            stsToken.setExpiryDate(parse);
            stsToken.setAccessKeyId(response.getCredentials().getAccessKeyId());
            stsToken.setAccessKeySecret(response.getCredentials().getAccessKeySecret());
            stsToken.setToken(response.getCredentials().getSecurityToken());

            return stsToken;
        } catch (ClientException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("SameParameterValue")
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

    public List<StsInfoVo> getCachedStsInfo() {

        String stsKeyPrefix = String.format(STS_KEY, "");

        CacheEngine cacheEngine = getCacheEngine();
        if (cacheEngine == null) {
            return new ArrayList<>();
        }

        List<String> keys = cacheEngine.scanCachePrefix(stsKeyPrefix);

        String cacheLocation = cacheEngine instanceof RedisCacheEngine ? "redis" : "local";

        List<StsInfoVo> list = new ArrayList<>();
        for (String key : keys) {
            AliyunStsToken stsToken = cacheEngine.get(key);
            if (stsToken == null) {
                continue;
            }
            long expireSeconds = cacheEngine.getExpireSeconds(key);

            StsInfoVo stsInfoVo = new StsInfoVo();
            String accessKeyId = stsToken.getAccessKeyId();
            stsInfoVo.setStsKeyId(fuzzy(accessKeyId, 10, 6));
            stsInfoVo.setStsToken(fuzzy(stsToken.getToken(), 10, 6));
            stsInfoVo.setCacheLocation(cacheLocation);
            stsInfoVo.setStsSessionName(stsToken.getRoleSessionName());
            stsInfoVo.setExpiredAt(stsToken.getExpiryDate());

            if (expireSeconds != -1) {
                stsInfoVo.setExpireSeconds((int) expireSeconds);
            }
            list.add(stsInfoVo);
        }

        return list;
    }

    public void clearStsTokenCache() {
        CacheEngine cacheEngine = getCacheEngine();
        if (cacheEngine == null) {
            return;
        }
        String stsKeyPrefix = String.format(STS_KEY, "");
        List<String> keys = cacheEngine.scanCachePrefix(stsKeyPrefix);
        if (keys != null) {
            for (String key : keys) {
                cacheEngine.remove(key);
            }
        }
    }

    public static void main(String[] args) {
        AliyunStsTokenProvider aliyunStsTokenProvider = AliyunStsTokenProvider.getInstance();

        AliyunStsKey key = AliyunStsKey.builder()
                .accessKeyId("LTxxxxxxxxxxxxxxuo")
                .accessKeySecret("GduxxxxxxxxxxxxxxxgA")
                .regionId("cn-shenzhen")
                .endpoint("sts.cn-shenzhen.aliyuncs.com")
                .roleArn("acs:ram::xxxxxxxxxxx:role/xxxxxxxxxxx")
                .roleSessionName("test-session")
                .build();

        AliyunStsToken token = aliyunStsTokenProvider.acquireToken(key);

        System.out.println(token);
    }
}
