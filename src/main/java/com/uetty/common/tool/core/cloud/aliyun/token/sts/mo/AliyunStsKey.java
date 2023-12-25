package com.uetty.common.tool.core.cloud.aliyun.token.sts.mo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AliyunStsKey {

    /**
     * 区域
     */
    @JSONField(serialize = false, deserialize = false)
    private String regionId;
    /**
     * 接入点（一般接入点确定区域也确定了，但是区域确定介入点不一定，至少有内网、外网两种接入点）
     */
    private String endpoint;
    /**
     * ak
     */
    private String accessKeyId;
    /**
     * sk
     */
    private String accessKeySecret;
    /**
     * 扮演的角色的arn
     */
    private String roleArn;
    /**
     * 给定sessionName，同一个扮演角色可能在不同地方被使用，可用该字段区分
     */
    private String roleSessionName;
    /**
     * 授权策略，默认为空时为角色最大权限，可通过该字段缩小权限
     */
    private String policy;
    /**
     * 设置一个最大的有效期时间，防止返回的异常数据或者时间格式异常，错误读取到超长过期时间，导致实际已过期却为删除token缓存
     */
    @JSONField(serialize = false, deserialize = false)
    @Builder.Default
    private int maxDurationSeconds = 3600;
}
