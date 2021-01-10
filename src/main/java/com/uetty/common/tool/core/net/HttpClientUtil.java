package com.uetty.common.tool.core.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uetty.common.tool.core.json.fastxml.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class HttpClientUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final int DEF_CONN_TIMEOUT = 30_000;
    private static final int DEF_READ_TIMEOUT = 30_000;

    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    // 将map型转为请求参数型
    @SuppressWarnings("unchecked")
    private static String buildParams(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        if (data == null) return sb.toString();
        for (Map.Entry<String, Object> item : data.entrySet()) {
            String key = item.getKey();
            Object value = item.getValue();
            if (key == null) continue;
            if (value == null) value = "";

            if (value instanceof List) {
                List<Object> valList = (List<Object>) value;
                for (Object val : valList) {
                    addParam(sb, key, val);
                }
            } else {
                addParam(sb, key, value);
            }
        }
        if (sb.length() > 0) sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    private static NameValuePair newNameValuePair(String key, Object value) {
        String val = value == null ? "" : value.toString();
        return new BasicNameValuePair(key, val);
    }

    private static BasicHeader newBasicHeader(String key, Object value) {
        if (key == null) return null;
        key = key.toLowerCase();
        if (value == null) value = "";
        return new BasicHeader(key, value.toString());
    }

    private static void addParam(StringBuilder sb, String key, Object value) {
        if (value == null) value = "";
        try {
            sb.append(URLEncoder.encode(key, DEFAULT_CHARSET)).append("=");
            sb.append(URLEncoder.encode(value.toString(), DEFAULT_CHARSET)).append("&");
        } catch (UnsupportedEncodingException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void addParams(List<NameValuePair> pairs, String key, Object value) {
        if (key == null) return;
        if (value == null) value = "";
        if (value instanceof List) {
            List<Object> valList = (List<Object>) value;
            for (Object val : valList) {
                pairs.add(newNameValuePair(key, val));
            }
        } else {
            pairs.add(newNameValuePair(key, value));
        }
    }

    private static boolean isContentTypeJson(String contentType) {
        return CONTENT_TYPE_JSON.equalsIgnoreCase(contentType);
    }

    private static HttpEntity createEntity(Map<String, Object> params, String contentType) throws UnsupportedEncodingException, JsonProcessingException {
        boolean contentTypeJson = isContentTypeJson(contentType);
        if (contentTypeJson) {
            return new JsonStringEntity(params);
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
            Header contentTypeHeader = httpRequest.getFirstHeader(HTTP.CONTENT_TYPE);

            HttpEntity entity = createEntity(params, contentTypeHeader != null ? contentTypeHeader.getValue() : DEFAULT_CONTENT_TYPE);

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
            if (StringUtils.isBlank(key)) continue;
            Object value = entry.getValue();
            if (value == null) value = "";
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

    private static void setHeaders(HttpMessage httpMessage, Map<String, Object> headers) {
        List<Header> list = buildHeaders(headers);
        String contentType = null;
        for (Header header : list) {
            if (HTTP.CONTENT_TYPE.equalsIgnoreCase(header.getName())) {
                contentType = header.getValue();
                break;
            }
        }
        if (contentType == null) {
            BasicHeader basicHeader = newBasicHeader(HTTP.CONTENT_TYPE, contentType);
            list.add(basicHeader);
        }
        if (list.size() == 0) return;
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
            if (name == null) continue;
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

    private static HttpGet createGet(String uri, Map<String, Object> headers, Map<String, Object> params) {
        String paramStr = buildParams(params);
        if (paramStr.length() > 0) {
            if (uri.indexOf('?') == -1) {
                uri += '?';
            } else {
                uri += '&';
            }
            uri += paramStr;
        }
        HttpGet httpGet = new HttpGet(uri);
        setHeaders(httpGet, headers);
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
        HttpGet httpGet = createGet(uri, headers, params);
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

    public static HttpResponseVo doGetLoad(String uri, Map<String, Object> headers, Map<String, Object> params) {
        HttpGet httpGet = createGet(uri, headers, params);

        return doLoad(httpGet);
    }

    public static HttpResponseVo doPostLoad(String uri, Map<String, Object> headers, Map<String, Object> params) {
        HttpPost httpPost = createPost(uri, headers, params);

        return doLoad(httpPost);
    }

    private static HttpResponseVo doLoad(HttpUriRequest request) {
        HttpResponseVo hrr = new HttpResponseVo();
        HttpClientBuilder httpClientBuilder = getHttpClientBuilder();

        try (CloseableHttpClient httpClient = httpClientBuilder.build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int code = response.getStatusLine().getStatusCode();
            Map<String, List<String>> headers = getResponseHeaders(response);
            HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);

            hrr.setCode(code);
            hrr.setHeaders(headers);
            hrr.setInputStream(bufferedHttpEntity.getContent());

            List<String> dispositions = headers.get(CONTENT_DISPOSITION);
            if (dispositions != null && dispositions.size() > 0 && dispositions.get(0).contains("filename=")) {
                String fileNameDisp = dispositions.get(0);
                String fileName = fileNameDisp.substring(fileNameDisp.indexOf("filename=") + 9);
                if (fileName.length() > 0 && !"".equals(fileName.trim())) hrr.setFileName(fileName);
            }

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return hrr;
    }
    public static class HttpResponseVo {

        private Integer code;
        private Map<String, List<String>> headers;
        private String body;
        private InputStream inputStream;
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

        public InputStream getInputStream() {
            return inputStream;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
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
            super(JacksonUtil.jackson.obj2Json(parameters), ContentType.APPLICATION_JSON);
        }
    }


}
