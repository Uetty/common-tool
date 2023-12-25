package com.uetty.common.tool.core.cloud.aliyun.token;

import com.uetty.common.tool.core.cloud.aliyun.token.mo.AliyunAccessToken;

/**
 * @author vince
 */
public interface TokenProvider<K> {

    AliyunAccessToken getAccessToken(K key);
}
