package com.uetty.common.tool.core.net;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: Vince
 * @Date: 2019/6/6 18:11
 */
public class HttpClientUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);

    private static final String DEF_CHATSET = "UTF-8";
    private static final int DEF_CONN_TIMEOUT = 30_000;
    private static final int DEF_READ_TIMEOUT = 30_000;
    private static final String DEF_CONTENT_TYPE = "Content-Type:application/json";

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
        if (value == null) value = "";
        return new BasicHeader(key, value.toString());
    }

    private static void addParam(StringBuilder sb, String key, Object value) {
        if (value == null) value = "";
        try {
            sb.append(URLEncoder.encode(key, DEF_CHATSET)).append("=");
            sb.append(URLEncoder.encode(value.toString(), DEF_CHATSET)).append("&");
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

    private static void setEntity(HttpPost httpPost, List<NameValuePair> nvps) {
        try {
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nvps, DEF_CHATSET);
            formEntity.setContentType(DEF_CONTENT_TYPE);
            httpPost.setEntity(formEntity);

        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static void setParams(HttpPost httpPost, Map<String, Object> params) {
        List<NameValuePair> nvps = new ArrayList<>();
        if (params == null || params.size() == 0) {
            setEntity(httpPost, nvps);
            return;
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            addParams(nvps, entry.getKey(), entry.getValue());
        }
        setEntity(httpPost, nvps);
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
                    headerList.add(newBasicHeader(key, val));
                }
            } else {
                headerList.add(newBasicHeader(key, value));
            }
        }
        return headerList;
    }

    private static void setHeaders(HttpMessage httpMessage, Map<String, Object> headers) {
        List<Header> list = buildHeaders(headers);
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
        setParams(httpPost, params);
        setHeaders(httpPost, headers);
        httpPost.setConfig(getRequestConfig());
        return httpPost;
    }

    private static HttpGet createGet(String uri, Map<String, Object> headers, Map<String, Object> params) {
        String paramStr = buildParams(params);
        if (paramStr.length() > 0) {
            uri = uri + "?" + paramStr;
        }
        HttpGet httpGet = new HttpGet(uri);
        setHeaders(httpGet, headers);
        httpGet.setConfig(getRequestConfig());
        return httpGet;
    }

    private static HttpResponseVo doRequest(HttpUriRequest request) {
        HttpResponseVo hrr = new HttpResponseVo();
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        try (CloseableHttpClient httpClient = httpClientBuilder.build();
             CloseableHttpResponse response = httpClient.execute(request)) {

            int code = response.getStatusLine().getStatusCode();
            Map<String, List<String>> headers = getResponseHeaders(response);
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity, DEF_CHATSET);

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

    public static class HttpResponseVo {
        private Integer code;
        private Map<String, List<String>> headers;
        private String body;

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
    }
}
