package com.uetty.common.tool.core.cloud.aliyun.token.sts.mo;

import lombok.Data;

import java.util.Date;

/**
 * @author vince
 */
@Data
public class StsInfoVo {

    private String stsKeyId;

    private String stsToken;

    private String cacheLocation;

    private String stsSessionName;

    private Date expiredAt;

    private Integer expireSeconds;
}
