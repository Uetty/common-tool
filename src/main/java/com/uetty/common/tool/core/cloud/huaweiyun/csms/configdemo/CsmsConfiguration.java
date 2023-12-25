package com.uetty.common.tool.core.cloud.huaweiyun.csms.configdemo;

import com.uetty.common.tool.core.cloud.aliyun.redis.properties.KmsRedisProperties;
import com.uetty.common.tool.core.cloud.huaweiyun.csms.properties.CsmsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@ConditionalOnProperty(name = "csms.enable", havingValue = "true")
@Configuration
public class CsmsConfiguration {

    @Bean
    public CsmsProperties kmsProperties() {
        return new CsmsProperties();
    }
}
