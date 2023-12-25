package com.uetty.common.tool.core.cloud.aliyun.kms.properties;

import lombok.Data;

@Data
public class KmsCredentialKey {

    private String secretName;

    private String regionId;
}
