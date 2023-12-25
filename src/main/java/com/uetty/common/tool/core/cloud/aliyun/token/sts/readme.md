
# 配置

需要`SpringContextPeeper`、`CacheManager`支持。
`SpringContextPeeper`用于动态获取RedisTemplate，如果启用了RedisTemplate，则会将token缓存到redis中;
`CacheManager`用于本地缓存

# 使用

```
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
AliyunStsToken stsToken = AliyunStsManager.getInstance().getStsToken(key);
```
