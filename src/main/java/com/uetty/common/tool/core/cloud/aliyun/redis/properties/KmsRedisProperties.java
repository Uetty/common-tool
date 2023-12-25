package com.uetty.common.tool.core.cloud.aliyun.redis.properties;

import com.uetty.common.tool.core.cloud.aliyun.kms.AliyunKmsManager;
import com.uetty.common.tool.core.cloud.aliyun.kms.properties.KmsRedisCredential;
import com.uetty.common.tool.core.spring.SpringContextPeeper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 使用kms管理redis密钥时，使用该类替代RedisProperties
 */
@Slf4j
@ConfigurationProperties(prefix = "spring.redis")
public class KmsRedisProperties extends RedisProperties {
    private static final long REFRESH_INTERVAL = 60 * 60 * 1000;

    private static volatile Boolean kmsRedisCredential;

    private static volatile long lastRefresh;

    private static volatile AliyunKmsManager aliyunKmsManager;

    /**
     * 是否从kms获取密钥
     */
    private boolean useKmsRedisCredential() {
        if (kmsRedisCredential != null) {
            return kmsRedisCredential;
        }
        boolean kmsEnable = SpringContextPeeper.getBooleanProperties("kms.enable");
        kmsRedisCredential = kmsEnable && SpringContextPeeper.getBooleanProperties("kms.redisSecret");
        return kmsRedisCredential;
    }

    private AliyunKmsManager getAliyunKmsManager() {
        if (aliyunKmsManager != null) {
            return aliyunKmsManager;
        }
        aliyunKmsManager = AliyunKmsManager.getInstance();
        return aliyunKmsManager;
    }

    private void refreshCredential() {
        boolean useKmsRedisCredential = useKmsRedisCredential();
        if (!useKmsRedisCredential) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long pass = currentTimeMillis - lastRefresh;
        if (pass < REFRESH_INTERVAL) {
            return;
        }

        AliyunKmsManager aliyunKmsManager = getAliyunKmsManager();
        if (!aliyunKmsManager.kmsEnabled()) {
            log.warn("aliyun kms manager not available");
            return;
        }

        log.debug("refresh redis credential");
        KmsRedisCredential kmsRedisCredential = aliyunKmsManager.acquireRedisCredential();
        setUsername(kmsRedisCredential.getUsername());
        setPassword(kmsRedisCredential.getPassword());

        lastRefresh = currentTimeMillis;
    }

    @Override
    public String getUsername() {
        refreshCredential();
        return super.getUsername();
    }

    @Override
    public String getPassword() {
        refreshCredential();
        return super.getPassword();
    }
}
