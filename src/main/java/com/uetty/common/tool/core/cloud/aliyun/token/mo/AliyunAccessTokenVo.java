package com.uetty.common.tool.core.cloud.aliyun.token.mo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
public class AliyunAccessTokenVo {

    private String accessKeyId;

    private String token;

    private String cacheLocation;

    private Date expiryDate;

    private Integer expireSeconds;
}
