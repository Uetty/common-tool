package com.uetty.common.tool.core.aliyun;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.Protocol;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.uetty.common.tool.core.Mimetypes;
import com.uetty.common.tool.core.string.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;

@Slf4j
public class AliOSSUtil {

    @Data
    private static class OSSEndpoint {
        String accessKey;
        String secret;
        String endpoint;
    }

    private static class OSSClientWrapper {

        OSSClient ossClient;

        @Override
        protected void finalize() {
            // shutdown before gc
            ossClient.shutdown();
        }
    }

    private static final WeakHashMap<OSSEndpoint, OSSClientWrapper> CLIENT_CACHE = new WeakHashMap<>();

    private static OSSClient getFromCache(OSSEndpoint endpoint) {
        synchronized (CLIENT_CACHE) {
            final OSSClientWrapper wrapper = CLIENT_CACHE.get(endpoint);
            if (wrapper != null) {
                return wrapper.ossClient;
            } else {
                return null;
            }
        }
    }

    private static void putClientCache(OSSEndpoint endpoint, OSSClient ossClient) {
        synchronized (CLIENT_CACHE) {
            OSSClientWrapper wrapper = new OSSClientWrapper();
            wrapper.ossClient = ossClient;
            CLIENT_CACHE.put(endpoint, wrapper);
        }
    }

    private static OSSEndpoint newOSSEndpoint(String accessKey, String secret, String endpoint) {
        OSSEndpoint ossEndpoint = new OSSEndpoint();
        ossEndpoint.setAccessKey(accessKey);
        ossEndpoint.setSecret(secret);
        ossEndpoint.setEndpoint(endpoint);
        return ossEndpoint;
    }

    /**
     * 初始化OSS
     */
    public static OSSClient obtainClient(String accessKey, String secret, String endpoint) {
        OSSEndpoint ossEndpoint = newOSSEndpoint(accessKey, secret, endpoint);
        // OSSClient 是线程安全的，所以可以缓存多线程共用
        OSSClient ossClient = getFromCache(ossEndpoint);
        if (ossClient == null) {
            CredentialsProvider credentialProvider = new DefaultCredentialProvider(accessKey, secret);
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            clientConfiguration.setProtocol(Protocol.HTTPS);
            ossClient = new OSSClient(endpoint, credentialProvider, clientConfiguration);
            putClientCache(ossEndpoint, ossClient);
        }
        return ossClient;
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
        final String userMetadataPrefix = OSSHeaders.OSS_USER_METADATA_PREFIX;
        int userMetadataPrefixLength = userMetadataPrefix.length();
        headers.forEach((key, value) -> {
            if (StringUtil.isBlank(key)) {
                return;
            }
            key = key.toLowerCase().trim();
            if (key.startsWith(OSSHeaders.OSS_USER_METADATA_PREFIX)) {
                if (key.length() > userMetadataPrefixLength) {
                    objectMetadata.addUserMetadata(key.substring(userMetadataPrefixLength), (value == null) ? "" : value.toString());
                }
            } else {
                objectMetadata.setHeader(key, value);
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
    public static PutObjectResult putObject(OSSClient ossClient, String bucketName, String keyName, File file, Map<String, Object> headers) {
        // file方式，ObjectMetadata会自动初始化：Content-Type、Content-Length这几个值
        // OSSHeaders.CONTENT_TYPE、OSSHeaders.CONTENT_LENGTH、OSSHeaders.CONTENT_DISPOSITION
        PutObjectRequest req = new PutObjectRequest(bucketName, keyName, file);
        ObjectMetadata objectMetadata = req.getMetadata();
        setContentType(objectMetadata, file);
        setHeader(objectMetadata,headers);
        return ossClient.putObject(req);
    }

    /**
     * 上传文件到OSS
     */
    public static PutObjectResult putObject(OSSClient ossClient, String bucketName, String keyName, File file) {
        return putObject(ossClient, bucketName, keyName, file, null);
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
    public static PutObjectResult putObject(OSSClient ossClient, String bucketName, String keyName, InputStream inputStream, Map<String, Object> headers) {
        // inputStream方式，如果未设置Content-Type，ObjectMetadata会自动根据keyName初始化Content-Type
        // 最好要设置 OSSHeaders.CONTENT_TYPE、OSSHeaders.CONTENT_LENGTH、OSSHeaders.CONTENT_DISPOSITION
        ObjectMetadata objectMetadata = new ObjectMetadata();
        setHeader(objectMetadata, headers);
        PutObjectRequest req = new PutObjectRequest(bucketName, keyName, inputStream, objectMetadata);

        return ossClient.putObject(req);
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
    public static OSSObject getObject(OSSClient ossClient, String bucketName, String keyName) {
        return ossClient.getObject(bucketName, keyName);
    }

    /**
     * 从OSS下载文件
     */
    public static OSSObject getObject(String accessKey, String secret, String endpoint, String bucketName, String keyName) {
        return getObject(obtainClient(accessKey, secret, endpoint), bucketName, keyName);
    }

    /**
     * 从OSS下载文件
     */
    public static InputStream getObjectInputStream(OSSClient ossClient, String bucketName, String keyName) {
        OSSObject ossObject = getObject(ossClient, bucketName, keyName);
        return ossObject.getObjectContent();
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
    public static String getPresignedUrl(OSSClient ossClient, String bucketName, String keyName, Date expirationDate) {
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, keyName);
        urlRequest.setExpiration(expirationDate);
        final URL url = ossClient.generatePresignedUrl(urlRequest);
        return url.toString();
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
    public static String getPresignedUrl(OSSClient ossClient, String bucketName, String keyName, long millisSeconds) {
        return getPresignedUrl(ossClient, bucketName, keyName, getExpirationTime(millisSeconds));
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
    public static void deleteObject(OSSClient ossClient, String bucketName, String keyName) {
        ossClient.deleteObject(bucketName, keyName);
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
    public static boolean doesObjectExist(OSSClient ossClient, String bucketName, String keyName) {
        return ossClient.doesObjectExist(bucketName, keyName);
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
