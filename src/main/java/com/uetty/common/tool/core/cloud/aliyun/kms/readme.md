

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

kms.rdsSecretName=xxxx-rds
# 根据实际键名配置，RDS轮转密钥默认为AccountName和AccountPassword
kms.rdsUserKey=AccountName
kms.rdsPasswordKey=AccountPassword
```
2. 需要SPI `com.uetty.common.tool.core.cloud.aliyun.kms.jdbc.KmsRdsProvider`支持
3. 使用重写的驱动 
```
spring.datasource.driver-class-name=com.uetty.common.tool.core.cloud.aliyun.kms.jdbc.AliyunKmsMysqlDriver
```
4. url使用新协议 `secrets-manager` 
```
spring.datasource.url=secrets-manager:mysql://rm-uf68xxxxxx397p.mysql.rds.aliyuncs.com:3306/xxxxx?useUnicode=true&characterEncoding=UTF-8&useSSL=false
```
