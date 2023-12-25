package com.uetty.common.tool.core.cloud.huaweiyun.token.delegation.mo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

/**
 * 通过Delegation获取token的参数配置
 * @author vince
 */
@Data
public class HuaweiyunDelegationConfig {

    /**
     * token获取请求地址（末尾%s为角色名）
     */
    private String tokenUrl = "http://169.254.169.254/openstack/latest/securitykey";
    /**
     * 设置一个最大的有效期时间，防止返回的异常数据或者时间格式异常，错误读取到超长过期时间，导致实际已过期却为删除token缓存
     */
    @JSONField(serialize = false, deserialize = false)
    private int maxDurationSeconds = 4 * 60 * 60;
}
