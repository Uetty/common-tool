package com.uetty.common.tool.core.cloud.huaweiyun.token;

import com.uetty.common.tool.core.cloud.huaweiyun.token.mo.HuaweiyunAccessToken;

/**
 * @author vince
 */
public interface TokenProvider<K> {

    HuaweiyunAccessToken getAccessToken(K key);
}
