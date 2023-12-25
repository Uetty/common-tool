package com.uetty.common.tool.core.cloud.huaweiyun.csms;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.csms.v1.CsmsClient;
import com.huaweicloud.sdk.csms.v1.model.ShowSecretVersionRequest;
import com.huaweicloud.sdk.csms.v1.model.ShowSecretVersionResponse;
import com.huaweicloud.sdk.csms.v1.model.Version;
import com.uetty.common.tool.core.cloud.huaweiyun.csms.properties.CsmsCredentialKey;
import com.uetty.common.tool.core.cloud.huaweiyun.csms.properties.CsmsProperties;
import com.uetty.common.tool.core.cloud.jdbc.KmsRdsCredential;
import com.uetty.common.tool.core.cloud.huaweiyun.token.TokenProvider;
import com.uetty.common.tool.core.cloud.huaweiyun.token.delegation.HuaweiyunDelegationTokenProvider;
import com.uetty.common.tool.core.cloud.huaweiyun.token.delegation.mo.HuaweiyunDelegationConfig;
import com.uetty.common.tool.core.cloud.huaweiyun.token.mo.HuaweiyunAccessToken;
import com.uetty.common.tool.core.spring.SpringContextPeeper;

/**
 * @author vince
 */
public class HuaweiyunCsmsManager {

    private TokenProvider<?> tokenProvider;

    private CsmsProperties csmsProperties;

    private static HuaweiyunCsmsManager SINGLETON;

    private volatile Boolean csmsEnabled;

    private HuaweiyunCsmsManager() {
    }

    public static HuaweiyunCsmsManager getInstance() {
        if (SINGLETON != null) {
            return SINGLETON;
        }
        synchronized (HuaweiyunCsmsManager.class) {
            if (SINGLETON != null) {
                return SINGLETON;
            }
            SINGLETON = new HuaweiyunCsmsManager();
            return SINGLETON;
        }
    }

    public static HuaweiyunCsmsManager getInstance(TokenProvider<?> tokenProvider) {
        if (SINGLETON != null) {
            return SINGLETON;
        }
        synchronized (HuaweiyunCsmsManager.class) {
            if (SINGLETON != null) {
                return SINGLETON;
            }
            SINGLETON = new HuaweiyunCsmsManager();
            SINGLETON.tokenProvider = tokenProvider;
            return SINGLETON;
        }
    }

    private TokenProvider<?> getTokenProvider() {
        if (tokenProvider != null) {
            return tokenProvider;
        }
        tokenProvider = HuaweiyunDelegationTokenProvider.getInstance();
        return tokenProvider;
    }

    public boolean csmsEnabled() {
        if (csmsEnabled != null) {
            return csmsEnabled;
        }

        synchronized (this) {
            if (csmsEnabled != null) {
                return csmsEnabled;
            }
            csmsEnabled = SpringContextPeeper.getBooleanProperties("csms.enable");
        }
        return csmsEnabled;
    }

    public CsmsProperties getCsmsProperties() {
        if (csmsProperties != null) {
            return csmsProperties;
        }
        csmsProperties = SpringContextPeeper.getBean(CsmsProperties.class);
        if (csmsProperties == null) {
            throw new RuntimeException("init CsmsProperties is null, please check csms.enable=true");
        }
        return csmsProperties;
    }

    public KmsRdsCredential acquireRdsCredential() {

        CsmsCredentialKey credentialKey = new CsmsCredentialKey();
        credentialKey.setProjectId(getCsmsProperties().getProjectId());
        credentialKey.setSecretName(getCsmsProperties().getRdsSecretName());

        String csmsCredentials = getCsmsCredentials(credentialKey);

        JSONObject jsonObject = JSON.parseObject(csmsCredentials);
        String username = jsonObject.getString(getCsmsProperties().getRdsUserKey());
        String password = jsonObject.getString(getCsmsProperties().getRdsPasswordKey());

        KmsRdsCredential credential = new KmsRdsCredential();
        credential.setUsername(username);
        credential.setPassword(password);

        return credential;
    }

    public String getCsmsCredentials(CsmsCredentialKey credentialKey) {

        TokenProvider<?> tokenProvider = getTokenProvider();
        if (tokenProvider instanceof HuaweiyunDelegationTokenProvider) {
            HuaweiyunDelegationConfig delegationConfig = new HuaweiyunDelegationConfig();
            delegationConfig.setTokenUrl(getCsmsProperties().getDelegationTokenUrl());
            if (getCsmsProperties().getDelegationMaxDurationSeconds() != null) {
                delegationConfig.setMaxDurationSeconds(getCsmsProperties().getDelegationMaxDurationSeconds());
            }

            HuaweiyunAccessToken accessToken = ((HuaweiyunDelegationTokenProvider) tokenProvider).getAccessToken(delegationConfig);
            return getCsmsCredentials(accessToken, credentialKey);
        }

        throw new RuntimeException("unknown token provider");
    }

    private String getCsmsCredentials(HuaweiyunAccessToken accessToken, CsmsCredentialKey csmsCredentialKey) {


        BasicCredentials auth = new BasicCredentials()
                .withAk(accessToken.getAccessKeyId())
                .withSk(accessToken.getAccessKeySecret())
                .withSecurityToken(accessToken.getToken())
                .withProjectId(csmsCredentialKey.getProjectId());

        CsmsClient csmsClient = CsmsClient.newBuilder()
                .withCredential(auth)
                .withEndpoint(getCsmsProperties().getEndpoint())
                .build();

        ShowSecretVersionRequest showSecretVersionRequest = new ShowSecretVersionRequest().withSecretName(csmsCredentialKey.getSecretName())
                .withVersionId("latest");

        ShowSecretVersionResponse version = csmsClient.showSecretVersion(showSecretVersionRequest);
        Version latestVersion = version.getVersion();

        return latestVersion.getSecretString();
    }

}
