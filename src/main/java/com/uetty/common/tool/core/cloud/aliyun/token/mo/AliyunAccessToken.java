package com.uetty.common.tool.core.cloud.aliyun.token.mo;

import lombok.Data;

import java.util.Date;

@Data
public class AliyunAccessToken {

    private String accessKeyId;

    private String accessKeySecret;

    private String token;

    private Date expiryDate;
}
