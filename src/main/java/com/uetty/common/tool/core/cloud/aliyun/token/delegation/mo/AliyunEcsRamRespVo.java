package com.uetty.common.tool.core.cloud.aliyun.token.delegation.mo;

import lombok.Data;

@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
@Data
public class AliyunEcsRamRespVo {

    private String AccessKeyId;

    private String AccessKeySecret;

    private String SecurityToken;

    private String Expiration;

    private String LastUpdated;

    private String Code;
}
