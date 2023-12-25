

## 配置

1. 启用kms，启用sts用于访问kms，检查KMS与STS配置
```
kms.enable=true
kms.regionId=cn-shanghai
kms.stsAccessKeyId=LTxxxxxxxxxxxxMws
kms.stsAccessSecret=ufxxxxxxxxxxxxxxxxxxxxxxxV7
kms.stsRegionId=cn-shenzhen
#kms.stsEndpoint=sts.cn-shenzhen.aliyuncs.com
kms.stsEndpoint=sts.cn-shenzhen.aliyuncs.com
# STS扮演角色
kms.stsRoleArn=acs:ram::195000000000000006477:role/kmsRolexxxxx
kms.stsDuration=3600
# 定义sessionname用于区分STS token作用
kms.stsSession=APP-KMS-Session

```
2. bean `KmsRedisProperties`仅使用KMS管理Redis密钥时配置
3. 其他密钥相关配置自行确定获取机制，作为入参调用`AliyunKmsManager.getInstance().getKmsCredentials(KmsCredentialKey)`获取各种凭据