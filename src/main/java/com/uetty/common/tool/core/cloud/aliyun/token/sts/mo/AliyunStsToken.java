package com.uetty.common.tool.core.cloud.aliyun.token.sts.mo;

import com.uetty.common.tool.core.cloud.aliyun.token.mo.AliyunAccessToken;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AliyunStsToken extends AliyunAccessToken {

    private String regionId;

    private String endpoint;

    private String roleSessionName;

    private String roleArn;

    private String policy;

}
