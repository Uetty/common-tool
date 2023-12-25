package com.uetty.common.tool.core.cloud.aliyun.kms.configdemo;

import com.uetty.common.tool.core.cloud.aliyun.kms.properties.KmsProperties;
import com.uetty.common.tool.core.cloud.aliyun.redis.properties.KmsRedisProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@ConditionalOnProperty(name = "kms.enable", havingValue = "true")
@Configuration
public class KmsConfiguration {

    @Primary
    @Bean
    KmsRedisProperties kmsRedisProperties() {
        return new KmsRedisProperties();
    }

    @Bean
    public KmsProperties kmsProperties() {
        return new KmsProperties();
    }
}
