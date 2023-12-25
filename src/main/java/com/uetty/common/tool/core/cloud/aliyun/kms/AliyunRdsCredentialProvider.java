package com.uetty.common.tool.core.cloud.aliyun.kms;


import com.uetty.common.tool.core.cloud.jdbc.KmsRdsCredential;
import com.uetty.common.tool.core.cloud.jdbc.RdsCredentialProvider;

public class AliyunRdsCredentialProvider implements RdsCredentialProvider {

    private static AliyunKmsManager kmsManager = AliyunKmsManager.getInstance();

    public static void setHoldKmsManager(AliyunKmsManager kmsManager) {
        AliyunRdsCredentialProvider.kmsManager = kmsManager;
    }

    @Override
    public KmsRdsCredential getRdsCredential(String group) {
        if (!kmsManager.kmsEnabled()) {
            throw new RuntimeException("aliyun kms manager not available");
        }
        return kmsManager.acquireRdsCredential();
    }

}
