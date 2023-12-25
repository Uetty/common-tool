package com.uetty.common.tool.core.cloud.huaweiyun.csms.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "csms")
public class CsmsProperties {

    private String endpoint;

    private String projectId;

    private String delegationTokenUrl;

    private Integer delegationMaxDurationSeconds = 4 * 60 * 60;

    private String rdsSecretName;

    private String rdsUserKey;

    private String rdsPasswordKey;

}
