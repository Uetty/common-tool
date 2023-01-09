package com.uetty.common.tool.core.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uetty.common.tool.core.json.fastxml.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class HttpRequestUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestUtil.class);

    private static final String DEF_CHATSET = "UTF-8";
    private static final int DEF_CONN_TIMEOUT = 30_000;
    private static final int DEF_READ_TIMEOUT = 30_000;

    private static final String CONTENT_TYPE_KEY = "Content-Type";
    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    private static void addParam(StringBuilder sb, String key, Object value) {
        if (value == null) {
            value = "";
        }
        try {
            sb.append(URLEncoder.encode(key, DEF_CHATSET)).append("=");
            sb.append(URLEncoder.encode(value.toString(), DEF_CHATSET)).append("&");
        } catch (UnsupportedEncodingException e) {
            LOG.warn(e.getMessage(), e);
        }
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

    private static String getContentType(Map<String, Object> headers) {
        String contentType = null;
        for (String key : headers.keySet()) {
            if (equalsIgnoreCase(key, CONTENT_TYPE_KEY)) {
                contentType = String.valueOf(headers.get(key));
            }
        }
        if (contentType == null) {
            return DEFAULT_CONTENT_TYPE;
        }
        return contentType;
    }

    private static void replaceContentType(Map<String, Object> headers, String contentType) {
        String contentTypeKey = null;
        for (String key : headers.keySet()) {
            if (equalsIgnoreCase(key, CONTENT_TYPE_KEY)) {
                contentTypeKey = key;
                break;
            }
        }

        if (contentTypeKey != null) {
            headers.remove(contentTypeKey);
        }
        headers.put(CONTENT_TYPE_KEY, contentType);
    }

    // 将map型转为请求参数型
    @SuppressWarnings("unchecked")
    private static Object buildParams(Map<String, Object> data, Map<String, Object> headers) {
        String contentType = getContentType(headers);
        if (isContentTypeJson(contentType)) {
            if (data == null) {
                return "{}";
            }
            try {
                return JacksonUtil.getDefault().obj2Json(data);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else if (isContentTypeMultipart(contentType)) {
            String boundary = getContentTypeBoundary(contentType);
            MultipartInputStream inputStream;
            if (boundary != null && boundary.length() > 0) {
                inputStream = new MultipartInputStream(boundary, data);
            } else {
                inputStream = new MultipartInputStream(null, data);
                // 更新 boundary
                boundary = inputStream.getBoundary();
                contentType = CONTENT_TYPE_MULTIPART + "; boundary=" + boundary;

                replaceContentType(headers, contentType);
            }

            return inputStream;
        } else {
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
    }

    @SuppressWarnings("unchecked")
    private static void addHeaders(HttpURLConnection conn, Map<String, Object> headers, String contentType) throws UnsupportedEncodingException {
        if (headers == null || headers.size() == 0) {
            return;
        }
        for (Map.Entry<String, Object> entry: headers.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null) {
                continue;
            }
            if (CONTENT_TYPE_KEY.equalsIgnoreCase(key)) {
                continue;
            }
            if (value == null) {
                value = "";
            }
            if (value instanceof List) {
                List<Object> lv = (List<Object>) value;
                for (Object val : lv) {
                    if (val == null) {
                        val = "";
                    }
                    conn.addRequestProperty(key, val.toString());
                }
            } else {
                conn.setRequestProperty(key, value.toString());
            }
        }
        conn.setRequestProperty(CONTENT_TYPE_KEY, contentType);
    }

    /**
     * 空值安全的startWith（忽略大小写）
     */
    private static boolean startsWithIgnoreCase(String str, String prefix) {
        return (str != null && prefix != null && str.length() >= prefix.length() &&
                str.regionMatches(true, 0, prefix, 0, prefix.length()));
    }

    private static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.toLowerCase().equalsIgnoreCase(str2.toLowerCase());
    }

    private static boolean isContentTypeJson(String contentType) {
        return startsWithIgnoreCase(contentType, CONTENT_TYPE_JSON);
    }

    private static boolean isContentTypeMultipart(String contentType) {
        return startsWithIgnoreCase(contentType, CONTENT_TYPE_MULTIPART);
    }

    private static HttpResponseVo doRequest(String uri, Method method, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        HttpURLConnection conn = null;
        BufferedReader reader = null;
        try {
            StringBuilder sb = new StringBuilder();
            if (headers == null) {
                headers = new HashMap<>();
            }
            if (params == null) {
                params = new HashMap<>();
            }
            Object param = buildParams(params, headers);

            String contentType = getContentType(headers);
            boolean contentTypeJson = isContentTypeJson(contentType);
            boolean contentTypeMultipart = isContentTypeMultipart(contentType);
            if (!contentTypeJson && !contentTypeMultipart) {
                uri = uri + (param.toString().length() > 0 ? "?" + param : "");
            }
            URL url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            if (method == null) {
                method = Method.GET;
            }
            conn.setRequestMethod(method.name());

            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            addHeaders(conn, headers, contentType);
            if (contentTypeJson || contentTypeMultipart) {
                conn.setDoOutput(true);
            }
            conn.connect();
            if (contentTypeJson) {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(String.valueOf(param).getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } else if (contentTypeMultipart) {

                MultipartInputStream inputStream = (MultipartInputStream) param;
                OutputStream outputStream = conn.getOutputStream();
                int read;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                outputStream.flush();
            }

            InputStream is = conn.getInputStream();
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

    public static HttpResponseVo doPut(String uri, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        return doRequest(uri, Method.PUT, headers, params);
    }

    public static HttpResponseVo doLoad(String uri, Method method, Consumer<InputStream> inputStreamConsumer, Map<String, Object> headers, Map<String, Object> params) throws IOException {
        HttpURLConnection conn = null;
        try {
            StringBuilder sb = new StringBuilder();
            if (headers == null) {
                headers = new HashMap<>();
            }
            if (params == null) {
                params = new HashMap<>();
            }
            Object param = buildParams(params, headers);

            String contentType = getContentType(headers);
            boolean contentTypeJson = isContentTypeJson(contentType);
            boolean contentTypeMultipart = isContentTypeMultipart(contentType);
            if (!contentTypeJson && !contentTypeMultipart) {
                uri = uri + (param.toString().length() > 0 ? "?" + param : "");
            }
            URL url = new URL(uri);
            conn = (HttpURLConnection) url.openConnection();
            if (method == null) {
                method = Method.GET;
            }
            conn.setRequestMethod(method.name());

            conn.setUseCaches(false);
            conn.setConnectTimeout(DEF_CONN_TIMEOUT);
            conn.setReadTimeout(DEF_READ_TIMEOUT);
            addHeaders(conn, headers, contentType);
            if (contentTypeJson || contentTypeMultipart) {
                conn.setDoOutput(true);
            }
            conn.connect();
            if (contentTypeJson) {
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(String.valueOf(param).getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            } else if (contentTypeMultipart) {

                MultipartInputStream inputStream = (MultipartInputStream) param;
                OutputStream outputStream = conn.getOutputStream();
                int read;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                outputStream.flush();
            }

            InputStream is = conn.getInputStream();
            HttpResponseVo hrr = new HttpResponseVo();
            hrr.setCode(conn.getResponseCode());
            hrr.setHeaders(conn.getHeaderFields());

            Map<String, List<String>> responseHeaders = hrr.getHeaders();
            List<String> dispositions = responseHeaders.get(CONTENT_DISPOSITION);
            if (dispositions != null && dispositions.size() > 0 && dispositions.get(0).contains("filename=")) {
                String fileNameDisp = dispositions.get(0);
                String fileName = fileNameDisp.substring(fileNameDisp.indexOf("filename=") + 9);
                if (fileName.length() > 0 && !"".equals(fileName.trim())) {
                    hrr.setFileName(fileName);
                }
            }

            inputStreamConsumer.accept(is);

            return hrr;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public enum Method {
        /**
         * HTTP GET
         */
        GET,
        /**
         * HTTP POST
         */
        POST,
        /**
         * HTTP PUT
         */
        PUT,
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
}
