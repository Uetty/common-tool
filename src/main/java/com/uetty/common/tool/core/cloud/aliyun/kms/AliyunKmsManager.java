package com.uetty.common.tool.core.cloud.aliyun.kms;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.FormatType;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.kms.model.v20160120.GetSecretValueRequest;
import com.aliyuncs.kms.model.v20160120.GetSecretValueResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.uetty.common.tool.core.cloud.aliyun.kms.properties.KmsCredentialKey;
import com.uetty.common.tool.core.cloud.aliyun.kms.properties.KmsProperties;
import com.uetty.common.tool.core.cloud.aliyun.kms.properties.KmsRedisCredential;
import com.uetty.common.tool.core.cloud.aliyun.token.sts.AliyunStsTokenProvider;
import com.uetty.common.tool.core.cloud.aliyun.token.sts.mo.AliyunStsKey;
import com.uetty.common.tool.core.cloud.aliyun.token.sts.mo.AliyunStsToken;
import com.uetty.common.tool.core.cloud.aliyun.token.TokenProvider;
import com.uetty.common.tool.core.cloud.aliyun.token.mo.AliyunAccessToken;
import com.uetty.common.tool.core.cloud.jdbc.KmsRdsCredential;
import com.uetty.common.tool.core.spring.SpringContextPeeper;

/**
 * @author vince
 */
public class AliyunKmsManager {

    private TokenProvider<?> tokenProvider;

    private KmsProperties kmsProperties;

    private static AliyunKmsManager SINGLETON;

    private volatile Boolean kmsEnabled;

    private AliyunKmsManager() {
    }

    public static AliyunKmsManager getInstance() {
        if (SINGLETON != null) {
            return SINGLETON;
        }
        synchronized (AliyunKmsManager.class) {
            if (SINGLETON != null) {
                return SINGLETON;
            }
            SINGLETON = new AliyunKmsManager();
            return SINGLETON;
        }
    }

    public static AliyunKmsManager getInstance(TokenProvider<?> tokenProvider) {
        if (SINGLETON != null) {
            return SINGLETON;
        }
        synchronized (AliyunKmsManager.class) {
            if (SINGLETON != null) {
                return SINGLETON;
            }
            SINGLETON = new AliyunKmsManager();
            SINGLETON.tokenProvider = tokenProvider;
            return SINGLETON;
        }
    }

    private TokenProvider<?> getTokenProvider() {
        if (tokenProvider != null) {
            return tokenProvider;
        }
        tokenProvider = AliyunStsTokenProvider.getInstance();
        return tokenProvider;
    }

    public boolean kmsEnabled() {
        if (kmsEnabled != null) {
            return kmsEnabled;
        }

        synchronized (this) {
            if (kmsEnabled != null) {
                return kmsEnabled;
            }
            kmsEnabled = SpringContextPeeper.getBooleanProperties("kms.enable");
        }
        return kmsEnabled;
    }

    public KmsProperties getKmsProperties() {
        if (kmsProperties != null) {
            return kmsProperties;
        }
        kmsProperties = SpringContextPeeper.getBean(KmsProperties.class);
        if (kmsProperties == null) {
            throw new RuntimeException("init KmsProperties is null, please check kms.enable=true");
        }
        return kmsProperties;
    }

    public KmsRdsCredential acquireRdsCredential() {

        KmsCredentialKey credentialKey = new KmsCredentialKey();
        credentialKey.setRegionId(getKmsProperties().getRegionId());
        credentialKey.setSecretName(getKmsProperties().getRdsSecretName());

        String kmsCredentials = getKmsCredentials(credentialKey);

        JSONObject jsonObject = JSON.parseObject(kmsCredentials);
        String username = jsonObject.getString(getKmsProperties().getRdsUserKey());
        String password = jsonObject.getString(getKmsProperties().getRdsPasswordKey());

        KmsRdsCredential credential = new KmsRdsCredential();
        credential.setUsername(username);
        credential.setPassword(password);

        return credential;
    }

    public KmsRedisCredential acquireRedisCredential() {
        KmsCredentialKey credentialKey = new KmsCredentialKey();
        credentialKey.setRegionId(getKmsProperties().getRegionId());
        credentialKey.setSecretName(getKmsProperties().getRedisSecretName());

        String kmsCredentials = getKmsCredentials(credentialKey);

        JSONObject jsonObject = JSON.parseObject(kmsCredentials);
        String username = jsonObject.getString(getKmsProperties().getRedisUserKey());
        String password = jsonObject.getString(getKmsProperties().getRedisPasswordKey());

        KmsRedisCredential credential = new KmsRedisCredential();
        credential.setUsername(username);
        credential.setPassword(password);

        return credential;
    }

    public String getKmsCredentials(KmsCredentialKey credentialKey) {

        TokenProvider<?> tokenProvider = getTokenProvider();
        if (tokenProvider instanceof AliyunStsTokenProvider) {
            AliyunStsKey key = AliyunStsKey.builder()
                    .accessKeyId(getKmsProperties().getStsAccessKeyId())
                    .accessKeySecret(getKmsProperties().getStsAccessSecret())
                    .regionId(getKmsProperties().getStsRegionId())
                    .endpoint(getKmsProperties().getStsEndpoint())
                    .roleArn(getKmsProperties().getStsRoleArn())
                    .roleSessionName(getKmsProperties().getStsSession())
                    .build();
            if (getKmsProperties().getStsDuration() != null) {
                key.setMaxDurationSeconds(getKmsProperties().getStsDuration());
            }

            AliyunAccessToken stsToken = ((AliyunStsTokenProvider) tokenProvider).getAccessToken(key);
            return getKmsCredentials(stsToken, credentialKey);
        }

        throw new RuntimeException("unknown token provider");
    }

    private static String getKmsCredentials(AliyunAccessToken accessToken, KmsCredentialKey kmsCredentialKey) {
        IClientProfile profile = DefaultProfile.getProfile(kmsCredentialKey.getRegionId(), accessToken.getAccessKeyId(), accessToken.getAccessKeySecret(), accessToken.getToken());
        DefaultAcsClient kmsClient = new DefaultAcsClient(profile);

        try {
            GetSecretValueRequest request = new GetSecretValueRequest();
            request.setSysProtocol(ProtocolType.HTTPS);
            request.setAcceptFormat(FormatType.JSON);
            request.setSysMethod(MethodType.POST);
            request.setSecretName(kmsCredentialKey.getSecretName());
            GetSecretValueResponse response = kmsClient.getAcsResponse(request);

            return response.getSecretData();
        } catch (ClientException e) {
            throw new RuntimeException(e);
        } finally {
            kmsClient.shutdown();
        }
    }

    public static void main(String[] args) {
        AliyunStsTokenProvider aliyunStsTokenProvider = AliyunStsTokenProvider.getInstance();

        AliyunStsKey key = AliyunStsKey.builder()
                .accessKeyId("LTAxxxxxxxxxxxxxxya")
                .accessKeySecret("6vxxxxxxxxxxxxQT")
                .regionId("cn-shanghai")
                .endpoint("sts.cn-shanghai.aliyuncs.com")
                .roleArn("acs:ram::xxxxxx:role/xxxxxxxxrole")
                .roleSessionName("test-xxx-session")
                .build();


        AliyunStsToken token = aliyunStsTokenProvider.acquireToken(key);
        System.out.println(token);

        KmsCredentialKey credentialKey = new KmsCredentialKey();
        credentialKey.setRegionId("cn-shanghai");
        credentialKey.setSecretName("xxxxxx-rds");
        String kmsCredentials = getKmsCredentials(token, credentialKey);

        System.out.println(kmsCredentials);

    }

}
