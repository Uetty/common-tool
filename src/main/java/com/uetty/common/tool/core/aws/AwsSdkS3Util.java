package com.uetty.common.tool.core.aws;

import com.uetty.common.tool.core.Mimetypes;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class AwsSdkS3Util {

    @Data
    private static class AwsRegion {
        String accessKey;
        String secret;
        Region region;
    }

    private static class S3ClientWrapper {

        S3Client s3Client;

        @Override
        protected void finalize() {
            try {
                // shutdown before gc
                s3Client.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    private static final String CONTENT_TYPE_KEY = "content-type";
    private static final WeakHashMap<AwsRegion, S3ClientWrapper> CLIENT_CACHE = new WeakHashMap<>();

    private static S3Client getFromCache(AwsRegion awsRegion) {
        synchronized (CLIENT_CACHE) {
            final S3ClientWrapper s3ClientWrapper = CLIENT_CACHE.get(awsRegion);
            if (s3ClientWrapper != null) {
                return s3ClientWrapper.s3Client;
            } else {
                return null;
            }
        }
    }

    private static void putClientCache(AwsRegion awsRegion, S3Client s3Client) {
        synchronized (CLIENT_CACHE) {
            S3ClientWrapper wrapper = new S3ClientWrapper();
            wrapper.s3Client = s3Client;
            CLIENT_CACHE.put(awsRegion, wrapper);
        }
    }

    private static AwsRegion newAwsRegion(String accessKey, String secret, String regionName) {
        Region regions = Region.of(regionName);
        AwsRegion awsRegion = new AwsRegion();
        awsRegion.setAccessKey(accessKey);
        awsRegion.setSecret(secret);
        awsRegion.setRegion(regions);
        return awsRegion;
    }

    /**
     * 初始化S3
     */
    public static S3Client obtainClient(String accessKey, String secret, String regionName) {
        AwsRegion awsRegion = newAwsRegion(accessKey, secret, regionName);
        // s3Client 是线程安全的，所以可以缓存多线程共用
        S3Client s3Client = getFromCache(awsRegion);
        if (s3Client == null) {
            AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secret);
            s3Client = S3Client.builder()
                    .region(awsRegion.region)
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            putClientCache(awsRegion, s3Client);
        }
        return s3Client;
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

    private static Map<String, String> buildHeaders(Map<String, Object> map) {
        Map<String, String> headers = new HashMap<>();
        if (map == null) {
            return headers;
        }
        Set<String> keyNames = new HashSet<>();
        map.forEach((key, value) -> {
            String lowerKey = key.toLowerCase();
            if (!keyNames.contains(lowerKey)) {
                headers.put(key, (value == null) ? "" : value.toString());
                keyNames.add(lowerKey);
            }
        });
        return headers;
    }
    /**
     * 上传文件到S3
     */
    public static PutObjectResponse putObject(S3Client amazonS3, String bucketName, String keyName, File file, Map<String, String> headers) {

        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .contentDisposition(file.getName())
                .contentType(getContentTypeByFileName(file));
        if (headers != null) {
            builder.metadata(headers);
        }

        final PutObjectRequest req = builder.build();
        final RequestBody requestBody = RequestBody.fromFile(file);
        return amazonS3.putObject(req, requestBody);
    }

    /**
     * 上传文件到S3
     */
    public static PutObjectResponse putObject(S3Client amazonS3, String bucketName, String keyName, File file) {
        return putObject(amazonS3, bucketName, keyName, file, null);
    }

    /**
     * 上传文件到S3
     */
    public static PutObjectResponse putObject(String accessKey, String secret, String regionName, String bucketName, String keyName, File file, Map<String, String> headers) {
        return putObject(obtainClient(accessKey, secret, regionName), bucketName, keyName, file, headers);
    }

    /**
     * 上传文件到S3
     */
    public static PutObjectResponse putObject(String accessKey, String secret, String regionName, String bucketName, String keyName, File file) {
        return putObject(accessKey, secret, regionName, bucketName, keyName, file, null);
    }

    /**
     * 上传文件到S3
     */
    public static PutObjectResponse putObject(S3Client amazonS3, String bucketName, String keyName, InputStream inputStream, long contentLength, Map<String, String> headers) {

        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName);
        if (headers != null) {
            // map 中的 content-type 取出来单独处理
            final Map<String, String> keyMap = headers.keySet()
                    .stream()
                    .collect(Collectors.toMap(String::toLowerCase, Function.identity(), (a1, a2) -> a1));

            String contentType = keyMap.containsKey(CONTENT_TYPE_KEY) ? headers.remove(keyMap.get(CONTENT_TYPE_KEY)) : null;
            if (contentType != null) {
                // 设置content-type
                builder.contentType(contentType);
            }
            // 设置其他请求头
            builder.metadata(headers);
        }

        final PutObjectRequest req = builder.build();
        final RequestBody requestBody = RequestBody.fromInputStream(inputStream, contentLength);

        return amazonS3.putObject(req, requestBody);
    }

    /**
     * 上传文件到S3
     */
    public static PutObjectResponse putObject(String accessKey, String secret, String regionName, String bucketName, String keyName, InputStream inputStream, long contentLength, Map<String, String> headers) {
        return putObject(obtainClient(accessKey, secret, regionName), bucketName, keyName, inputStream, contentLength, headers);
    }

    /**
     * 从S3下载文件
     */
    public static GetObjectResponse getObject(S3Client amazonS3, String bucketName, String keyName) {
        final ResponseInputStream<GetObjectResponse> objectInputStream = getObjectInputStream(amazonS3, bucketName, keyName);
        return objectInputStream.response();
    }

    /**
     * 从S3下载文件
     */
    public static GetObjectResponse getObject(String accessKey, String secret, String regionName, String bucketName, String keyName) {
        return getObject(obtainClient(accessKey, secret, regionName), bucketName, keyName);
    }

    /**
     * 从S3下载文件
     */
    public static ResponseInputStream<GetObjectResponse> getObjectInputStream(S3Client amazonS3, String bucketName, String keyName) {
        GetObjectRequest req = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();
        return amazonS3.getObject(req);
    }

    /**
     * 从S3下载文件
     */
    public static ResponseInputStream<GetObjectResponse> getObjectInputStream(String accessKey, String secret, String regionName, String bucketName, String keyName) {
        return getObjectInputStream(obtainClient(accessKey, secret, regionName), bucketName, keyName);
    }

}
