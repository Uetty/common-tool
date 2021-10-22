package com.uetty.common.tool.core.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.*;
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
public class AwsServiceS3Util {

    @Data
    private static class S3Region {
        String accessKey;
        String secret;
        Regions region;
    }

    private static class AmazonS3Wrapper {

        AmazonS3 amazonS3;

        @Override
        protected void finalize() {
            try {
                // shutdown before gc
                amazonS3.shutdown();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static final WeakHashMap<S3Region, AmazonS3Wrapper> CLIENT_CACHE = new WeakHashMap<>();

    private static AmazonS3 getFromCache(S3Region s3Region) {
        synchronized (CLIENT_CACHE) {
            final AmazonS3Wrapper amazonS3Wrapper = CLIENT_CACHE.get(s3Region);
            if (amazonS3Wrapper != null) {
                return amazonS3Wrapper.amazonS3;
            } else {
                return null;
            }
        }
    }

    private static void putClientCache(S3Region s3Region, AmazonS3 amazonS3) {
        synchronized (CLIENT_CACHE) {
            AmazonS3Wrapper wrapper = new AmazonS3Wrapper();
            wrapper.amazonS3 = amazonS3;
            CLIENT_CACHE.put(s3Region, wrapper);
        }
    }

    private static S3Region newS3Region(String accessKey, String secret, String regionName) {
        Regions regions = Regions.fromName(regionName);
        S3Region s3Region = new S3Region();
        s3Region.setAccessKey(accessKey);
        s3Region.setSecret(secret);
        s3Region.setRegion(regions);
        return s3Region;
    }

    /**
     * 初始化S3
     */
    public static AmazonS3 obtainClient(String accessKey, String secret, String regionName) {
        S3Region s3Region = newS3Region(accessKey, secret, regionName);
        // AmazonS3 是线程安全的，所以可以缓存多线程共用
        AmazonS3 amazonS3 = getFromCache(s3Region);
        if (amazonS3 == null) {
            AWSCredentials credentials = new BasicAWSCredentials(accessKey, secret);
            amazonS3 = AmazonS3ClientBuilder.standard()
                    .withRegion(s3Region.region)
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .build();

            putClientCache(s3Region, amazonS3);
        }
        return amazonS3;
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
        final String userMetadataPrefix = Headers.S3_USER_METADATA_PREFIX;
        int userMetadataPrefixLength = userMetadataPrefix.length();
        headers.forEach((key, value) -> {
            if (StringUtil.isBlank(key)) {
                return;
            }
            key = key.toLowerCase().trim();
            if (key.startsWith(Headers.S3_USER_METADATA_PREFIX)) {
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
     * 上传文件到S3
     */
    public static PutObjectResult putObject(AmazonS3 amazonS3, String bucketName, String keyName, File file, Map<String, Object> headers) {
        // file方式，ObjectMetadata会自动初始化：Content-Type、Content-Length、Content-MD5（Content-MD5看策略）这几个值
        // Headers.CONTENT_LENGTH、Headers.CONTENT_TYPE、Headers.CONTENT_MD5、Headers.CONTENT_DISPOSITION

        PutObjectRequest req = new PutObjectRequest(bucketName, keyName, file);
        ObjectMetadata objectMetadata = req.getMetadata();
        setContentType(objectMetadata, file);
        setHeader(objectMetadata, headers);
        return amazonS3.putObject(req);
    }

    /**
     * 上传文件到S3
     */
    public static PutObjectResult putObject(AmazonS3 amazonS3, String bucketName, String keyName, File file) {
        return putObject(amazonS3, bucketName, keyName, file, null);
    }

    /**
     * 上传文件到S3
     */
    public static PutObjectResult putObject(String accessKey, String secret, String regionName, String bucketName, String keyName, File file, Map<String, Object> headers) {
        return putObject(obtainClient(accessKey, secret, regionName), bucketName, keyName, file, headers);
    }

    /**
     * 上传文件到S3
     */
    public static PutObjectResult putObject(String accessKey, String secret, String regionName, String bucketName, String keyName, File file) {
        return putObject(accessKey, secret, regionName, bucketName, keyName, file, null);
    }

    /**
     * 上传文件到S3
     */
    public static PutObjectResult putObject(AmazonS3 amazonS3, String bucketName, String keyName, InputStream inputStream, Map<String, Object> headers) {
        // 最好要设置 Headers.CONTENT_LENGTH、Headers.CONTENT_TYPE、Headers.CONTENT_DISPOSITION
        ObjectMetadata objectMetadata = new ObjectMetadata();
        setHeader(objectMetadata, headers);
        PutObjectRequest req = new PutObjectRequest(bucketName, keyName, inputStream, objectMetadata);

        return amazonS3.putObject(req);
    }

    /**
     * 上传文件到S3
     */
    public static PutObjectResult putObject(String accessKey, String secret, String regionName, String bucketName, String keyName, InputStream inputStream, Map<String, Object> headers) {
        return putObject(obtainClient(accessKey, secret, regionName), bucketName, keyName, inputStream, headers);
    }

    /**
     * 从S3下载文件
     */
    public static S3Object getObject(AmazonS3 amazonS3, String bucketName, String keyName) {
        return amazonS3.getObject(bucketName, keyName);
    }

    /**
     * 从S3下载文件
     */
    public static S3Object getObject(String accessKey, String secret, String regionName, String bucketName, String keyName) {
        return getObject(obtainClient(accessKey, secret, regionName), bucketName, keyName);
    }

    /**
     * 从S3下载文件
     */
    public static S3ObjectInputStream getObjectInputStream(AmazonS3 amazonS3, String bucketName, String keyName) {
        S3Object s3Object = getObject(amazonS3, bucketName, keyName);
        return s3Object.getObjectContent();
    }

    /**
     * 从S3下载文件
     */
    public static S3ObjectInputStream getObjectInputStream(String accessKey, String secret, String regionName, String bucketName, String keyName) {
        return getObjectInputStream(obtainClient(accessKey, secret, regionName), bucketName, keyName);
    }

    /**
     * 获取有过期时间的url
     */
    public static String getPresignedUrl(AmazonS3 amazonS3, String bucketName, String keyName, Date expirationDate) {
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(bucketName, keyName);
        urlRequest.setExpiration(expirationDate);
        final URL url = amazonS3.generatePresignedUrl(urlRequest);
        return url.toString();
    }

    /**
     * 获取有过期时间的url
     */
    public static String getPresignedUrl(String accessKey, String secret, String regionName, String bucketName, String keyName,
                                         Date expirationDate) {
        return getPresignedUrl(obtainClient(accessKey, secret, regionName), bucketName, keyName, expirationDate);
    }

    private static Date getExpirationTime(long millisSeconds) {
        long expirationTimeMillis = System.currentTimeMillis() + millisSeconds;
        return new Date(expirationTimeMillis);
    }

    /**
     * 获取有过期时间的url
     */
    public static String getPresignedUrl(AmazonS3 amazonS3, String bucketName, String keyName, long millisSeconds) {
        return getPresignedUrl(amazonS3, bucketName, keyName, getExpirationTime(millisSeconds));
    }

    /**
     * 获取有过期时间的url
     */
    public static String getPresignedUrl(String accessKey, String secret, String regionName, String bucketName, String keyName,
                                         long millisSeconds) {
        return getPresignedUrl(accessKey,  secret, regionName, bucketName, keyName, getExpirationTime(millisSeconds));
    }

    /**
     * 获取公用的url
     */
    public static String getPublicS3Url(AmazonS3 amazonS3, String bucketName, String keyName) {
        final URL url = amazonS3.getUrl(bucketName, keyName);
        return url.toString();
    }

    /**
     * 获取公用的url
     */
    public static String getPublicS3Url(String accessKey, String secret, String regionName, String bucketName, String keyName) {
        return getPublicS3Url(obtainClient(accessKey, secret, regionName), bucketName, keyName);
    }

    /**
     * 从S3删除文件
     */
    public static void deleteObject(AmazonS3 amazonS3, String bucketName, String keyName) {
        amazonS3.deleteObject(bucketName, keyName);
    }

    /**
     * 从S3删除文件
     */
    public static void deleteObject(String accessKey, String secret, String regionName, String bucketName, String keyName) {
        deleteObject(obtainClient(accessKey, secret, regionName), bucketName, keyName);
    }

    /**
     * 判断资源是否存在
     */
    public static boolean doesObjectExist(AmazonS3 amazonS3, String bucketName, String keyName) {
        return amazonS3.doesObjectExist(bucketName, keyName);
    }

    /**
     * 判断资源是否存在
     */
    public static boolean doesObjectExist(String accessKey, String secret, String regionName, String bucketName, String keyName) {
        return doesObjectExist(obtainClient(accessKey, secret, regionName), bucketName, keyName);
    }

//    public static boolean isS3Url(String fileS3Url)   {
//        try {
//            URL url = new URL(fileS3Url);
//            return url.getHost().matches("^[a-zA-Z0-9-]+.s3\\.[a-zA-Z0-9-]+\\.amazonaws.*$");
//        } catch (MalformedURLException e) {
//            log.error("url is not malformed", e);
//        }
//        return false;
//    }

}
