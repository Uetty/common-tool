package com.uetty.common.tool.core.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class HttpRequestUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestUtil.class);

    private static final String DEF_CHATSET = "UTF-8";
    private static final int DEF_CONN_TIMEOUT = 30_000;
    private static final int DEF_READ_TIMEOUT = 30_000;

    private static void addParam(StringBuilder sb, String key, Object value) {
        if (value == null) value = "";
        try {
            sb.append(URLEncoder.encode(key, DEF_CHATSET)).append("=");
            sb.append(URLEncoder.encode(value.toString(), DEF_CHATSET)).append("&");
        } catch (UnsupportedEncodingException e) {
            LOG.warn(e.getMessage(), e);
        }
    }

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

    @SuppressWarnings("unchecked")
    private static void addHeaders(HttpURLConnection conn, Map<String, Object> headers) throws UnsupportedEncodingException {
        if (headers == null || headers.size() == 0) return;
        for (Map.Entry<String, Object> entry: headers.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null) continue;
            if (value == null) value = "";
            if (value instanceof List) {
                List<Object> lv = (List<Object>) value;
                for (Object val : lv) {
                    if (val == null) val = "";
                    conn.addRequestProperty(key, URLEncoder.encode(val.toString(), DEF_CHATSET));
                }
            } else {
                conn.setRequestProperty(key, URLEncoder.encode(value.toString(), DEF_CHATSET));
            }
        }
    }

    private static HttpResponseVo doRequest(String uri, Method method, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            StringBuilder sb = new StringBuilder();
            String paramStr = buildParams(params);
            if (method == null || Method.GET == method) {
                uri = uri + (paramStr.length() > 0 ? "?" + paramStr : "");
            }
            URL url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            if (method == null || Method.GET == method) {
                conn.setRequestMethod(Method.GET.name());
            } else {
                conn.setRequestMethod(Method.POST.name());
                conn.setDoOutput(true);
            }
            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            addHeaders(conn, headers);
            conn.connect();
            if (Method.POST  == method) {
                DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                out.writeBytes(paramStr);
            }

            InputStream is = conn.getResponseCode() == 200 ? conn.getInputStream() : conn.getErrorStream();
            reader = new BufferedReader(new InputStreamReader(is, DEF_CHATSET));
            String strRead;
            while ((strRead = reader.readLine()) != null) {
                sb.append(strRead).append("\n");
            }
            HttpResponseVo hrr = new HttpResponseVo();
            hrr.setCode(conn.getResponseCode());
            hrr.setBody(sb.toString());
            hrr.setHeaders(conn.getHeaderFields());
            return hrr;
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    @SuppressWarnings("unused")
    public static HttpResponseVo doPost(String uri, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        return doRequest(uri, Method.POST, headers, params);
    }

    @SuppressWarnings("unused")
    public static HttpResponseVo doGet(String uri, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        return doRequest(uri, Method.GET, headers, params);
    }

    public enum Method {
        GET,
        POST,
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
