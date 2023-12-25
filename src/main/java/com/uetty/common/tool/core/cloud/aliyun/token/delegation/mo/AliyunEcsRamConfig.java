package com.uetty.common.tool.core.cloud.aliyun.token.delegation.mo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通过ECS RAM获取token的参数配置
 * @author vince
 */
@Data
public class AliyunEcsRamConfig {

    /**
     * 是否有元数据加固（元数据加固过的机器，能防止SSRF，当前仅能通过调用阿里云API方式开启强制要求加固，一般不使用）
     */
    private boolean metaReinforce;

    /**
     * meta token信息获取请求的地址
     * <p>仅元信息加固时是必须的</p>
     */
    private String metaUrl = "http://100.100.100.200/latest/api/token";
    /**
     * meta token请求头ttl字段
     */
    private String metaUrlHeaderTtl = "X-aliyun-ecs-metadata-token-ttl-seconds";

    /**
     * meta token请求头ttl默认值
     */
    private int defaultTtl = 300;

    /**
     * token获取请求地址（末尾%s为角色名）
     */
    private String tokenUrl = "http://100.100.100.200/latest/meta-data/ram/security-credentials/%s";
    /**
     * token获取请求头meta字段
     */
    private String tokenUrlHeaderMeta = "X-aliyun-ecs-metadata-token";
    /**
     * token获取使用的角色名
     */
    private String ramRoleName;
    /**
     * 设置一个最大的有效期时间，防止返回的异常数据或者时间格式异常，错误读取到超长过期时间，导致实际已过期却为删除token缓存
     */
    @JSONField(serialize = false, deserialize = false)
    private int maxDurationSeconds = 4 * 60 * 60;
}
