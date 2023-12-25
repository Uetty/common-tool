package com.uetty.common.tool.core.cloud.huaweiyun.token.mo;

import lombok.Data;

import java.util.Date;

@Data
public class HuaweiyunAccessToken {

    private String accessKeyId;

    private String accessKeySecret;

    private String token;

    private Date expiryDate;
}
