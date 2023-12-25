package com.uetty.common.tool.core.cloud.aliyun.kms.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kms")
public class KmsProperties {

    private String stsAccessKeyId;

    private String stsAccessSecret;

    private String stsRegionId;

    private String stsEndpoint;

    private String stsRoleArn;

    private Integer stsDuration;

    private String stsSession;

    private String ecsRamUrl;

    private String ecsRamRoleArn;

    private String regionId;

    private String rdsSecretName;

    private String rdsUserKey;

    private String rdsPasswordKey;

    private Boolean redisSecret = false;

    private String redisSecretName;

    private String redisUserKey;

    private String redisPasswordKey;
}
