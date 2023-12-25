package com.uetty.common.tool.core.cloud.huaweiyun.token.delegation.mo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class HuaweiyunDelegationRespVo {

    private CredentialBody credential;

    @Data
    public static class CredentialBody {
        private String access;

        private String secret;

        private String securitytoken;

        @JSONField(name = "expires_at")
        private String expiresAt;

    }

    public String getAccess() {
        if (credential == null) {
            return null;
        }
        return credential.getAccess();
    }

    public String getSecret() {
        if (credential == null) {
            return null;
        }
        return credential.getSecret();
    }

    public String getSecuritytoken() {
        if (credential == null) {
            return null;
        }
        return credential.getSecuritytoken();
    }

    public String getExpiresAt() {
        if (credential == null) {
            return null;
        }
        return credential.getExpiresAt();
    }
}
