package com.uetty.common.tool.core.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uetty.common.tool.core.json.fastxml.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpMessage;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class HttpClientUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final int DEF_CONN_TIMEOUT = 30_000;
    private static final int DEF_READ_TIMEOUT = 30_000;

    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    // 将map型转为请求参数型
    @SuppressWarnings("unchecked")
    private static String buildParams(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        if (data == null) {
            return sb.toString();
        }
        for (Map.Entry<String, Object> item : data.entrySet()) {
            String key = item.getKey();
            Object value = item.getValue();
            if (key == null) {
                continue;
            }
            if (value == null) {
                value = "";
            }

            if (value instanceof List) {
                List<Object> valList = (List<Object>) value;
                for (Object val : valList) {
                    addParam(sb, key, val);
                }
            } else {
                addParam(sb, key, value);
            }
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }

    private static NameValuePair newNameValuePair(String key, Object value) {
        String val = value == null ? "" : value.toString();
        return new BasicNameValuePair(key, val);
    }

    private static BasicHeader newBasicHeader(String key, Object value) {
        if (key == null) {
            return null;
        }
        key = key.toLowerCase();
        if (value == null) {
            value = "";
        }
        return new BasicHeader(key, value.toString());
    }

    private static void addParam(StringBuilder sb, String key, Object value) {
        if (value == null) {
            value = "";
        }
        try {
            sb.append(URLEncoder.encode(key, DEFAULT_CHARSET)).append("=");
            sb.append(URLEncoder.encode(value.toString(), DEFAULT_CHARSET)).append("&");
        } catch (UnsupportedEncodingException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addParams(List<NameValuePair> pairs, String key, Object value) {
        if (key == null) {
            return;
        }
        if (value == null) {
            value = "";
        }
        if (value instanceof List) {
            List<Object> valList = (List<Object>) value;
            for (Object val : valList) {
                pairs.add(newNameValuePair(key, val));
            }
        } else {
            pairs.add(newNameValuePair(key, value));
        }
    }

    /**
     * 空值安全的startWith（忽略大小写）
     */
    private static boolean startsWithIgnoreCase(String str, String prefix) {
        return (str != null && prefix != null && str.length() >= prefix.length() &&
                str.regionMatches(true, 0, prefix, 0, prefix.length()));
    }

    private static boolean isContentTypeJson(String contentType) {
        return startsWithIgnoreCase(contentType, CONTENT_TYPE_JSON);
    }

    private static boolean isContentTypeMultipart(String contentType) {
        return startsWithIgnoreCase(contentType, CONTENT_TYPE_MULTIPART);
    }
    
    private static String getContentTypeBoundary(String contentType) {
        String[] params = contentType.split(";");
        if (params.length < 2) {
            return null;
        }
        for (String param : params) {
            String[] split = param.trim().split("=");
            if (split.length < 2) {
                continue;
            }
            if ("boundary".equalsIgnoreCase(split[0])) {
                return split[1].trim();
            }
        }
        return null;
    }

    private static HttpEntity createEntity(HttpEntityEnclosingRequestBase httpRequest, Map<String, Object> params) throws UnsupportedEncodingException, JsonProcessingException {

        Header contentTypeHeader = httpRequest.getFirstHeader(HTTP.CONTENT_TYPE);
        String contentType = contentTypeHeader != null ? contentTypeHeader.getValue() : DEFAULT_CONTENT_TYPE;

        if (isContentTypeJson(contentType)) {
            return new JsonStringEntity(params);
        } else if (isContentTypeMultipart(contentType)) {
            String boundary = getContentTypeBoundary(contentType);
            MultipartEntity multipartEntity;
            if (boundary != null && boundary.length() > 0) {
                multipartEntity = new MultipartEntity(boundary, params);
            } else {
                multipartEntity = new MultipartEntity(null, params);
                // 更新 boundary
                boundary = multipartEntity.getBoundary();
                contentType = CONTENT_TYPE_MULTIPART + "; boundary=" + boundary;
                BasicHeader basicHeader = newBasicHeader(HTTP.CONTENT_TYPE, contentType);
                httpRequest.setHeader(basicHeader);
            }
            return multipartEntity;
        } else {
            List<NameValuePair> nvps = new ArrayList<>();
            if (params != null && params.size() > 0) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    addParams(nvps, entry.getKey(), entry.getValue());
                }
            }
            return new UrlEncodedFormEntity(nvps, DEFAULT_CONTENT_TYPE);
        }
    }

    private static void setParams(HttpEntityEnclosingRequestBase httpRequest, Map<String, Object> params) {
        try {

            HttpEntity entity = createEntity(httpRequest, params);

            httpRequest.setEntity(entity);

        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Header> buildHeaders(Map<String, Object> headers) {
        List<Header> headerList = new ArrayList<>();
        if (headers == null || headers.size() == 0) {
            return headerList;
        }
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.isBlank(key)) {
                continue;
            }
            Object value = entry.getValue();
            if (value == null) {
                value = "";
            }
            if (value instanceof List) {
                List<Object> valList = (List<Object>) value;
                for (Object val : valList) {
                    BasicHeader basicHeader = newBasicHeader(key, val);
                    if (basicHeader != null) {
                        headerList.add(basicHeader);
                    }
                }
            } else {
                BasicHeader basicHeader = newBasicHeader(key, value);
                if (basicHeader != null) {
                    headerList.add(basicHeader);
                }
            }
        }
        return headerList;
    }

    private static String getContentType(Map<String, Object> headers) {
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            if (HTTP.CONTENT_TYPE.equalsIgnoreCase(entry.getKey())) {
                Object value = entry.getValue();
                return value == null ? null : String.valueOf(value);
            }
        }
        return null;
    }

    private static void setHeaders(HttpMessage httpMessage, Map<String, Object> headers) {
        List<Header> list = buildHeaders(headers);
        String contentType = null;
        for (int i = 0; i < list.size(); i++) {
            Header header = list.get(i);
            if (HTTP.CONTENT_TYPE.equalsIgnoreCase(header.getName())) {
                contentType = header.getValue();
                list.remove(i);
                break;
            }
        }
        if (contentType == null) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        BasicHeader basicHeader = newBasicHeader(HTTP.CONTENT_TYPE, contentType);
        list.add(basicHeader);

        httpMessage.setHeaders(list.toArray(new Header[0]));
    }

    private static RequestConfig getRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(DEF_CONN_TIMEOUT).setConnectionRequestTimeout(DEF_CONN_TIMEOUT)
                .setSocketTimeout(DEF_READ_TIMEOUT).build();
    }

    private static Map<String, List<String>> getResponseHeaders(CloseableHttpResponse response) {
        Header[] headers = response.getAllHeaders();
        Map<String, List<String>> headerMap = new HashMap<>();
        for (Header header : headers) {
            String name = header.getName();
            if (name == null) {
                continue;
            }
            List<String> values = headerMap.computeIfAbsent(name, k -> new ArrayList<>());
            values.add(header.getValue());
        }
        return headerMap;
    }

    private static HttpPost createPost(String uri, Map<String, Object> headers, Map<String, Object> params) {
        HttpPost httpPost = new HttpPost(uri);
        setHeaders(httpPost, headers);
        setParams(httpPost, params);
        httpPost.setConfig(getRequestConfig());
        return httpPost;
    }

    private static HttpGetWithEntity createGet(String uri, Map<String, Object> headers, Map<String, Object> params) {
        String paramStr = buildParams(params);
        if (paramStr.length() > 0) {
            if (uri.indexOf('?') == -1) {
                uri += '?';
            } else {
                uri += '&';
            }
            uri += paramStr;
        }
        HttpGetWithEntity httpGet = new HttpGetWithEntity(uri);
        setHeaders(httpGet, headers);
        String contentType = getContentType(headers);
        boolean contentTypeMultipart = isContentTypeMultipart(contentType);
        boolean contentTypeJson = isContentTypeJson(contentType);
        if (contentTypeJson || contentTypeMultipart) {
            setParams(httpGet, params);
        }
        httpGet.setConfig(getRequestConfig());
        return httpGet;
    }

    private static HttpPut createPut(String uri, Map<String, Object> headers, Map<String, Object> params) {
        HttpPut httpPut = new HttpPut(uri);
        setHeaders(httpPut, headers);
        setParams(httpPut, params);
        httpPut.setConfig(getRequestConfig());
        return httpPut;
    }

    private static HttpDelete createDelete(String uri, Map<String, Object> headers, Map<String, Object> params) {
        String paramStr = buildParams(params);
        if (paramStr.length() > 0) {
            if (uri.indexOf('?') == -1) {
                uri += '?';
            } else {
                uri += '&';
            }
            uri += paramStr;
        }
        HttpDelete httpDelete = new HttpDelete(uri);
        setHeaders(httpDelete, headers);
        httpDelete.setConfig(getRequestConfig());
        return httpDelete;
    }

    private static HttpClientBuilder getHttpClientBuilder() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        setProxy(httpClientBuilder);
        return httpClientBuilder;
    }

    private static void setProxy(HttpClientBuilder httpClientBuilder) {
        String proxyHost = System.getProperty("proxy.host");
        String proxyPort = System.getProperty("proxy.port");
        Integer port = null;
        try {
            port = proxyPort == null ? null : Integer.valueOf(proxyPort);
        } catch (Exception ignore) {}
        if (proxyHost != null && port != null) {
            HttpHost proxy = new HttpHost(proxyHost, port);
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            httpClientBuilder.setRoutePlanner(routePlanner);
        }
    }

    private static HttpResponseVo doRequest(HttpUriRequest request) {
        HttpResponseVo hrr = new HttpResponseVo();
        HttpClientBuilder httpClientBuilder = getHttpClientBuilder();

        try (CloseableHttpClient httpClient = httpClientBuilder.build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int code = response.getStatusLine().getStatusCode();
            Map<String, List<String>> headers = getResponseHeaders(response);
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity, DEFAULT_CHARSET);

            hrr.setCode(code);
            hrr.setHeaders(headers);
            hrr.setBody(body);

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return hrr;
    }

    public static HttpResponseVo doPost(String uri, Map<String, Object> headers, Map<String, Object> params) {
        HttpPost httpPost = createPost(uri, headers, params);
        return doRequest(httpPost);
    }

    public static HttpResponseVo doGet(String uri, Map<String, Object> headers, Map<String, Object> params) {
        HttpGetWithEntity httpGet = createGet(uri, headers, params);
        return doRequest(httpGet);
    }

    public static HttpResponseVo doPut(String uri, Map<String, Object> headers, Map<String, Object> params) {
        HttpPut httpPut = createPut(uri, headers, params);
        return doRequest(httpPut);
    }

    public static HttpResponseVo doDelete(String uri, Map<String, Object> headers, Map<String, Object> params) {
        HttpDelete httpDelete = createDelete(uri, headers, params);
        return doRequest(httpDelete);
    }

    public static HttpResponseVo doGetLoad(String uri, Consumer<InputStream> inputStreamConsumer, Map<String, Object> headers, Map<String, Object> params) {
        HttpGetWithEntity httpGet = createGet(uri, headers, params);

        return doLoad(httpGet, inputStreamConsumer);
    }

    public static HttpResponseVo doPostLoad(String uri, Consumer<InputStream> inputStreamConsumer, Map<String, Object> headers, Map<String, Object> params) {
        HttpPost httpPost = createPost(uri, headers, params);

        return doLoad(httpPost, inputStreamConsumer);
    }

    private static HttpResponseVo doLoad(HttpUriRequest request, Consumer<InputStream> inputStreamConsumer) {
        HttpResponseVo hrr = new HttpResponseVo();
        HttpClientBuilder httpClientBuilder = getHttpClientBuilder();

        try (CloseableHttpClient httpClient = httpClientBuilder.build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int code = response.getStatusLine().getStatusCode();
            Map<String, List<String>> headers = getResponseHeaders(response);
            HttpEntity entity = response.getEntity();

            hrr.setCode(code);
            hrr.setHeaders(headers);

            List<String> dispositions = headers.get(CONTENT_DISPOSITION);
            if (dispositions != null && dispositions.size() > 0 && dispositions.get(0).contains("filename=")) {
                String fileNameDisp = dispositions.get(0);
                String fileName = fileNameDisp.substring(fileNameDisp.indexOf("filename=") + 9);
                if (fileName.length() > 0 && !"".equals(fileName.trim())) {
                    hrr.setFileName(fileName);
                }
            }

            inputStreamConsumer.accept(entity.getContent());

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return hrr;
    }

    /**
     * 违反正统规范的GET请求包含请求体
     */
    private static class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {

        public static final String METHOD_NAME = "GET";

        public HttpGetWithEntity() {
            super();
        }

        public HttpGetWithEntity(final URI uri) {
            super();
            setURI(uri);
        }

        /**
         * @throws IllegalArgumentException if the uri is invalid.
         */
        public HttpGetWithEntity(final String uri) {
            super();
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return METHOD_NAME;
        }
    }

    public static class HttpResponseVo {

        private Integer code;
        private Map<String, List<String>> headers;
        private String body;
        private String fileName;

        @SuppressWarnings("unused")
        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        @SuppressWarnings("unused")
        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, List<String>> headers) {
            this.headers = headers;
        }

        @SuppressWarnings("unused")
        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }

    private static class JsonStringEntity extends StringEntity {

        public JsonStringEntity(Map<String, Object> parameters) throws JsonProcessingException {
            super(JacksonUtil.getDefault().obj2Json(parameters), ContentType.APPLICATION_JSON);
        }
    }

    private static class MultipartEntity extends InputStreamEntity {


        public MultipartEntity(String boundary, Map<String, Object> params) {
            super(new MultipartInputStream(boundary, params));
        }

        public String getBoundary() {
            try {
                MultipartInputStream content = (MultipartInputStream) super.getContent();

                return content.getBoundary();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
