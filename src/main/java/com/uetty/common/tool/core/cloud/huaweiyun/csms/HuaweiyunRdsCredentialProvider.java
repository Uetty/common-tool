package com.uetty.common.tool.core.cloud.huaweiyun.csms;


import com.uetty.common.tool.core.cloud.jdbc.KmsRdsCredential;
import com.uetty.common.tool.core.cloud.jdbc.RdsCredentialProvider;

public class HuaweiyunRdsCredentialProvider implements RdsCredentialProvider {

    private static HuaweiyunCsmsManager csmsManager = HuaweiyunCsmsManager.getInstance();

    public static void setHoldKmsManager(HuaweiyunCsmsManager csmsManager) {
        HuaweiyunRdsCredentialProvider.csmsManager = csmsManager;
    }

    @Override
    public KmsRdsCredential getRdsCredential(String group) {
        if (!csmsManager.csmsEnabled()) {
            throw new RuntimeException("huaweiyun csms manager not available");
        }
        return csmsManager.acquireRdsCredential();
    }

}
