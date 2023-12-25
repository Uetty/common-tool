package com.uetty.common.tool.core.cloud.huaweiyun.obs;

import com.obs.services.BasicObsCredentialsProvider;
import com.obs.services.ObsClient;
import com.obs.services.ObsConfiguration;
import com.obs.services.internal.ObsHeaders;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.HttpProtocolTypeEnum;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.PutObjectRequest;
import com.obs.services.model.PutObjectResult;
import com.obs.services.model.SpecialParamEnum;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;
import com.uetty.common.tool.core.DateUtil;
import com.uetty.common.tool.core.Mimetypes;
import com.uetty.common.tool.core.string.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@Slf4j
public class HuaweiyunObsUtil {

    @Data
    private static class ObsEndpoint {
        String accessKey;
        String secret;
        String endpoint;
    }

    private static class ObsClientWrapper {

        ObsClient obsClient;

        @Override
        protected void finalize() {
            // shutdown before gc
            try {
                obsClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final WeakHashMap<ObsEndpoint, ObsClientWrapper> CLIENT_CACHE = new WeakHashMap<>();

    private static ObsClient getFromCache(ObsEndpoint endpoint) {
        synchronized (CLIENT_CACHE) {
            final ObsClientWrapper wrapper = CLIENT_CACHE.get(endpoint);
            if (wrapper != null) {
                return wrapper.obsClient;
            } else {
                return null;
            }
        }
    }

    private static void putClientCache(ObsEndpoint endpoint, ObsClient obsClient) {
        synchronized (CLIENT_CACHE) {
            ObsClientWrapper wrapper = new ObsClientWrapper();
            wrapper.obsClient = obsClient;
            CLIENT_CACHE.put(endpoint, wrapper);
        }
    }

    private static ObsEndpoint newOSSEndpoint(String accessKey, String secret, String endpoint) {
        ObsEndpoint obSEndpoint = new ObsEndpoint();
        obSEndpoint.setAccessKey(accessKey);
        obSEndpoint.setSecret(secret);
        obSEndpoint.setEndpoint(endpoint);
        return obSEndpoint;
    }

    /**
     * 初始化OSS
     */
    public static ObsClient obtainClient(String accessKey, String secret, String endpoint) {
        ObsEndpoint obSEndpoint = newOSSEndpoint(accessKey, secret, endpoint);
        // ObsClient 是线程安全的，所以可以缓存多线程共用
        ObsClient obsClient = getFromCache(obSEndpoint);
        if (obsClient == null) {
            BasicObsCredentialsProvider credentialProvider = new BasicObsCredentialsProvider(accessKey, secret);
            ObsConfiguration obsConfiguration = new ObsConfiguration();
            obsConfiguration.setEndPoint(endpoint);
            obsClient = new ObsClient(credentialProvider, obsConfiguration);
            putClientCache(obSEndpoint, obsClient);
        }
        return obsClient;
    }

    public static ObsClient obtainClientBySts(String accessKey, String secret, String stsToken, String endpoint) {
        BasicObsCredentialsProvider credentialProvider = new BasicObsCredentialsProvider(accessKey, secret, stsToken);
        ObsConfiguration obsConfiguration = new ObsConfiguration();
        obsConfiguration.setEndPoint(endpoint);
        // STS 方式的由于token一段时间变化一次，这里的缓存功能不够强大，不适合在这里加缓存
        return new ObsClient(credentialProvider, obsConfiguration);
    }

    /**
     * 根据文件名获取Content-Type
     */
    public static String getContentTypeByFileName(String fileName) {
        return Mimetypes.getInstance().getMimetype(fileName);
    }

    /**
     * 获取文件Content-Type（根据文件名）
     */
    public static String getContentTypeByFileName(File file) {
        return Mimetypes.getInstance().getMimetype(file);
    }

    private static void setHeader(ObjectMetadata objectMetadata, Map<String, Object> headers) {
        if (headers == null) {
            return;
        }
        final String userMetadataPrefix = ObsHeaders.getInstance().headerPrefix();
        int userMetadataPrefixLength = userMetadataPrefix.length();
        headers.forEach((key, value) -> {
            if (StringUtil.isBlank(key)) {
                return;
            }
            key = key.toLowerCase().trim();
            if (key.startsWith(userMetadataPrefix)) {
                if (key.length() > userMetadataPrefixLength) {
                    objectMetadata.addUserMetadata(key.substring(userMetadataPrefixLength), (value == null) ? "" : value.toString());
                }
            } else {
                objectMetadata.addUserMetadata(key, (value == null) ? "" : value.toString());
            }
        });
    }

    private static void setContentType(ObjectMetadata objectMetadata, File file) {
        // aws s3和阿里oss mime.types文件格式不一样，不能兼容，这里使用自定义mime.types文件解析进行取代
        String contentType = getContentTypeByFileName(file);
        objectMetadata.setContentType(contentType);
    }

    /**
     * 上传文件到OSS
     */
    public static PutObjectResult putObject(ObsClient obsClient, String bucketName, String keyName, File file, Map<String, Object> headers) {
        // file方式，ObjectMetadata会自动初始化：Content-Type、Content-Length这几个值
        // OSSHeaders.CONTENT_TYPE、OSSHeaders.CONTENT_LENGTH、OSSHeaders.CONTENT_DISPOSITION
        PutObjectRequest req = new PutObjectRequest(bucketName, keyName, file);
        ObjectMetadata objectMetadata = req.getMetadata();
        setContentType(objectMetadata, file);
        setHeader(objectMetadata, headers);

        return obsClient.putObject(req);
    }

    /**
     * 上传文件到OSS
     */
    public static PutObjectResult putObject(ObsClient obsClient, String bucketName, String keyName, File file) {
        return putObject(obsClient, bucketName, keyName, file, null);
    }

    /**
     * 上传文件到OSS
     */
    public static PutObjectResult putObject(String accessKey, String secret, String endpoint, String bucketName, String keyName, File file, Map<String, Object> headers) {
        return putObject(obtainClient(accessKey, secret, endpoint), bucketName, keyName, file, headers);
    }

    /**
     * 上传文件到OSS
     */
    public static PutObjectResult putObject(String accessKey, String secret, String endpoint, String bucketName, String keyName, File file) {
        return putObject(accessKey, secret, endpoint, bucketName, keyName, file, null);
    }

    /**
     * 上传文件到OSS
     */
    public static PutObjectResult putObject(ObsClient obsClient, String bucketName, String keyName, InputStream inputStream, Map<String, Object> headers) {
        // inputStream方式，如果未设置Content-Type，ObjectMetadata会自动根据keyName初始化Content-Type
        // 最好要设置 OSSHeaders.CONTENT_TYPE、OSSHeaders.CONTENT_LENGTH、OSSHeaders.CONTENT_DISPOSITION
        ObjectMetadata objectMetadata = new ObjectMetadata();
        setHeader(objectMetadata, headers);

        PutObjectRequest req = new PutObjectRequest(bucketName, keyName, inputStream);
        req.setMetadata(objectMetadata);

        return obsClient.putObject(req);
    }

    /**
     * 上传文件到OSS
     */
    public static PutObjectResult putObject(String accessKey, String secret, String endpoint, String bucketName, String keyName, InputStream inputStream, Map<String, Object> headers) {
        return putObject(obtainClient(accessKey, secret, endpoint), bucketName, keyName, inputStream, headers);
    }

    /**
     * 从OSS下载文件
     */
    public static ObsObject getObject(ObsClient obsClient, String bucketName, String keyName) {
        return obsClient.getObject(bucketName, keyName);
    }

    /**
     * 从OSS下载文件
     */
    public static ObsObject getObject(String accessKey, String secret, String endpoint, String bucketName, String keyName) {
        return getObject(obtainClient(accessKey, secret, endpoint), bucketName, keyName);
    }

    /**
     * 从OSS下载文件
     */
    public static InputStream getObjectInputStream(ObsClient obsClient, String bucketName, String keyName) {
        ObsObject obsObject = getObject(obsClient, bucketName, keyName);
        return obsObject.getObjectContent();
    }

    /**
     * 从OSS下载文件
     */
    public static InputStream getObjectInputStream(String accessKey, String secret, String endpoint, String bucketName, String keyName) {
        return getObjectInputStream(obtainClient(accessKey, secret, endpoint), bucketName, keyName);
    }

    /**
     * 获取有过期时间的url
     */
    public static String getPresignedUrl(ObsClient obsClient, String bucketName, String keyName, Date expirationDate) {

        long durationSeconds = DateUtil.getDurationSeconds(new Date(), expirationDate);
        TemporarySignatureRequest urlRequest = new TemporarySignatureRequest(HttpMethodEnum.GET, durationSeconds);
        urlRequest.setBucketName(bucketName);
        urlRequest.setObjectKey(keyName);
        TemporarySignatureResponse temporarySignature = obsClient.createTemporarySignature(urlRequest);
        return temporarySignature.getSignedUrl();
    }

    /**
     * 获取有过期时间的url
     */
    public static String getPresignedUrl(String accessKey, String secret, String endpoint, String bucketName, String keyName,
                                         Date expirationDate) {
        return getPresignedUrl(obtainClient(accessKey, secret, endpoint), bucketName, keyName, expirationDate);
    }

    private static Date getExpirationTime(long millisSeconds) {
        long expirationTimeMillis = System.currentTimeMillis() + millisSeconds;
        return new Date(expirationTimeMillis);
    }

    /**
     * 获取有过期时间的url
     */
    public static String getPresignedUrl(ObsClient obsClient, String bucketName, String keyName, long millisSeconds) {
        return getPresignedUrl(obsClient, bucketName, keyName, getExpirationTime(millisSeconds));
    }

    /**
     * 获取有过期时间的url
     */
    public static String getPresignedUrl(String accessKey, String secret, String endpoint, String bucketName, String keyName,
                                         long millisSeconds) {
        return getPresignedUrl(accessKey,  secret, endpoint, bucketName, keyName, getExpirationTime(millisSeconds));
    }

    /**
     * 获取公共url （存储桶需要是公共读权限）
     */
    public static String getPublicUrl(String accessKey, String secret, String endpoint, String bucketName, String keyName) {
        return "https://" + bucketName + "." + endpoint + "/" + keyName;
    }

    /**
     * 从OSS删除文件
     */
    public static void deleteObject(ObsClient obsClient, String bucketName, String keyName) {
        obsClient.deleteObject(bucketName, keyName);
    }

    /**
     * 从OSS删除文件
     */
    public static void deleteObject(String accessKey, String secret, String endpoint, String bucketName, String keyName) {
        deleteObject(obtainClient(accessKey, secret, endpoint), bucketName, keyName);
    }

    /**
     * 判断资源是否存在
     */
    public static boolean doesObjectExist(ObsClient obsClient, String bucketName, String keyName) {
        return obsClient.doesObjectExist(bucketName, keyName);
    }

    /**
     * 判断资源是否存在
     */
    public static boolean doesObjectExist(String accessKey, String secret, String endpoint, String bucketName, String keyName) {
        return doesObjectExist(obtainClient(accessKey, secret, endpoint), bucketName, keyName);
    }

    public static void main(String[] args) {
        String presignedUrl = getPublicUrl("xxxxxxxx", "xxxxxxxxxxxxxxxxxx",
                "oss-cn-xxxxx.aliyuncs.com", "xxxxxxx",
                "xxxx");
        System.out.println(presignedUrl);
    }
}
