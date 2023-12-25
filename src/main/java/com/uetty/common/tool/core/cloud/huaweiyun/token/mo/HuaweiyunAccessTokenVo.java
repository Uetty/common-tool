package com.uetty.common.tool.core.cloud.huaweiyun.token.mo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
public class HuaweiyunAccessTokenVo {

    private String accessKeyId;

    private String token;

    private String cacheLocation;

    private Date expiryDate;

    private Integer expireSeconds;
}
