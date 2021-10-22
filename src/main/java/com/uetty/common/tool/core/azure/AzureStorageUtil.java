package com.uetty.common.tool.core.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobProperties;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import com.uetty.common.tool.core.Mimetypes;
import com.uetty.common.tool.core.string.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

@Slf4j
public class AzureStorageUtil {

    private static final String CONNECTION_STRING_FORMAT = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=%s";

    private static final String HEADER_CACHE_CONTROL = "cache-control";
    private static final String HEADER_CONTENT_ENCODING = "content-encoding";
    private static final String HEADER_CONTENT_DISPOSITION = "content-disposition";
    private static final String HEADER_CONTENT_LANGUAGE = "content-language";
    private static final String HEADER_CONTENT_TYPE = "content-type";
    private static final String HEADER_CONTENT_MD5 = "content-md5";


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

    private static void setHeader(BlobProperties properties, Map<String, Object> headers) {
        if (headers == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            String headerName = entry.getKey();
            if (StringUtil.isBlank(headerName)) {
                continue;
            }
            headerName = headerName.toLowerCase();
            String value = entry.getValue() == null ? null : entry.getValue().toString().trim();
            switch (headerName) {
                case HEADER_CACHE_CONTROL:
                    properties.setCacheControl(value);
                    break;
                case HEADER_CONTENT_ENCODING:
                    properties.setContentEncoding(value);
                    break;
                case HEADER_CONTENT_DISPOSITION:
                    properties.setContentDisposition(value);
                    break;
                case HEADER_CONTENT_LANGUAGE:
                    properties.setContentLanguage(value);
                    break;
                case HEADER_CONTENT_TYPE:
                    properties.setContentType(value);
                    break;
                case HEADER_CONTENT_MD5:
                    properties.setContentMD5(value);
                    break;
            }
        }
    }

    private static void setContentType(BlobProperties properties, File file) {
        String contentType = getContentTypeByFileName(file);
        properties.setContentType(contentType);
    }

    /**
     * 上传文件
     */
    public static void uploadBlob(String accountName, String accountKey, String endpointSuffix,
                              String containerName, String blobName, File file, Map<String, Object> headers) {
        final String connectionString = String.format(CONNECTION_STRING_FORMAT, accountName, accountKey, endpointSuffix);
        try {
            final CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
            final CloudBlobClient cloudBlobClient = account.createCloudBlobClient();
            final CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
            final CloudBlockBlob blockBlob = container.getBlockBlobReference(blobName);
            final BlobProperties properties = blockBlob.getProperties();
            setContentType(properties, file);
            setHeader(properties, headers);

            try (InputStream inputStream = new FileInputStream(file)) {
                blockBlob.upload(inputStream, file.length());
            }
        } catch (Exception e) {
            throw new RuntimeException("[azure] upload blob failed", e);
        }
    }

    /**
     * 上传文件
     */
    public static void uploadBlob(String accountName, String accountKey, String endpointSuffix,
                              String containerName, String blobName, File file) {
        uploadBlob(accountName, accountKey, endpointSuffix, containerName, blobName, file, null);
    }

    /**
     * 上传文件
     */
    public static void uploadBlob(String accountName, String accountKey, String endpointSuffix, String containerName,
                                  String blobName, InputStream inputStream, long length, Map<String, Object> headers) {
        final String connectionString = String.format(CONNECTION_STRING_FORMAT, accountName, accountKey, endpointSuffix);
        try {
            final CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
            final CloudBlobClient cloudBlobClient = account.createCloudBlobClient();
            final CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
            final CloudBlockBlob blockBlob = container.getBlockBlobReference(blobName);
            final BlobProperties properties = blockBlob.getProperties();
            setHeader(properties, headers);

            blockBlob.upload(inputStream, length);
        } catch (Exception e) {
            throw new RuntimeException("[azure] upload blob failed", e);
        }
    }

    /**
     * 上传文件
     */
    public static void uploadBlob(String accountName, String accountKey, String endpointSuffix, String containerName,
                                  String blobName, InputStream inputStream, Map<String, Object> headers) {
        long length;
        try {
            length = inputStream.available();
        } catch (Exception e) {
            throw new RuntimeException("[azure] upload blob failed", e);
        }
        uploadBlob(accountName, accountKey, endpointSuffix, containerName, blobName, inputStream, length, headers);
    }

    /**
     * 下载文件
     */
    public static void downloadBlob(String accountName, String accountKey, String endpointSuffix, String containerName, String blobName, OutputStream outputStream) {
        final String connectionString = String.format(CONNECTION_STRING_FORMAT, accountName, accountKey, endpointSuffix);
        try {
            final CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
            final CloudBlobClient cloudBlobClient = account.createCloudBlobClient();
            final CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
            final CloudBlockBlob blockBlob = container.getBlockBlobReference(blobName);

            blockBlob.download(outputStream);
        } catch (Exception e) {
            throw new RuntimeException("[azure] download blob failed", e);
        }
    }

    /**
     * 下载文件
     */
    public static InputStream downloadBlob(String accountName, String accountKey, String endpointSuffix, String containerName, String blobName) {
        final String connectionString = String.format(CONNECTION_STRING_FORMAT, accountName, accountKey, endpointSuffix);
        try {
            final CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
            final CloudBlobClient cloudBlobClient = account.createCloudBlobClient();
            final CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
            final CloudBlockBlob blockBlob = container.getBlockBlobReference(blobName);

            return blockBlob.openInputStream();
        } catch (Exception e) {
            throw new RuntimeException("[azure] download blob failed", e);
        }
    }

    /**
     * 获取有过期时间的url
     */
    public static String getPresignedUrl(String accountName, String accountKey, String endpointSuffix, String containerName, String blobName, Date expirationDate) {
        final String connectionString = String.format(CONNECTION_STRING_FORMAT, accountName, accountKey, endpointSuffix);
        try {
            final CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
            final CloudBlobClient cloudBlobClient = account.createCloudBlobClient();
            final CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
            final CloudBlockBlob blockBlob = container.getBlockBlobReference(blobName);

            final SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
            policy.setSharedAccessExpiryTime(expirationDate);

            String queryString = blockBlob.generateSharedAccessSignature(policy, null);
            return blockBlob.getUri().toString() + "?" + queryString;
        } catch (Exception e) {
            throw new RuntimeException("[azure] get presigned url failed", e);
        }
    }

    private static Date getExpirationTime(long millisSeconds) {
        long expirationTimeMillis = System.currentTimeMillis() + millisSeconds;
        return new Date(expirationTimeMillis);
    }

    /**
     * 获取有过期时间的url
     */
    public static String getPresignedUrl(String accountName, String accountKey, String endpointSuffix, String containerName, String blobName, long millisSeconds) {
        return getPresignedUrl(accountName, accountKey, endpointSuffix, containerName, blobName, getExpirationTime(millisSeconds));
    }

    /**
     * 删除文件
     */
    public static void deleteBlob(String accountName, String accountKey, String endpointSuffix, String containerName, String blobName) {
        final String connectionString = String.format(CONNECTION_STRING_FORMAT, accountName, accountKey, endpointSuffix);
        try {
            final CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
            final CloudBlobClient cloudBlobClient = account.createCloudBlobClient();
            final CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
            final CloudBlockBlob blockBlob = container.getBlockBlobReference(blobName);

            blockBlob.deleteIfExists();
        } catch (Exception e) {
            throw new RuntimeException("[azure] delete blob failed", e);
        }
    }

    /**
     * 判断资源是否存在
     */
    public static boolean doesObjectExist(String accountName, String accountKey, String endpointSuffix, String containerName, String blobName) {
        final String connectionString = String.format(CONNECTION_STRING_FORMAT, accountName, accountKey, endpointSuffix);
        try {
            final CloudStorageAccount account = CloudStorageAccount.parse(connectionString);
            final CloudBlobClient cloudBlobClient = account.createCloudBlobClient();
            final CloudBlobContainer container = cloudBlobClient.getContainerReference(containerName);
            final CloudBlockBlob blockBlob = container.getBlockBlobReference(blobName);

            return blockBlob.exists();
        } catch (Exception e) {
            throw new RuntimeException("[azure] check blob exists failed", e);
        }
    }
}
