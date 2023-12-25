package com.uetty.common.tool.core.cloud.jdbc;

public interface RdsCredentialProvider {

    /**
     * 获取rds凭据
     * @param group 预留分组参数，用于应对特殊场景需求
     */
    KmsRdsCredential getRdsCredential(String group);

}
