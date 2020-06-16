package com.uetty.common.tool.core.api.github;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.uetty.common.tool.core.net.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 */
public class GithubApi {
    private static final Logger LOG = LoggerFactory.getLogger(GithubApi.class);

    private static final long GET_TOKEN_DELAY = 3000L;
    private static final String CHARSET = "UTF-8";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36";
    private static final String ACCEPT_TEXT_MATCH_MATEDATA = "application/vnd.github.v3.text-match+json";
	
	private static final String GITHUB_API_SEARCH_CODE = "https://api.github.com/search/code";

    private static final int CODE_SUCCESS = 200;
    private static final int CODE_PERMANENT_REDIRECTION = 301; // 永久重定向
    private static final int CODE_TEMPORARY_REDIRECTION_1 = 302; // 临时重定向
    private static final int CODE_TEMPORARY_REDIRECTION_2 = 307; // 临时重定向
    private static final int CODE_JSON_ERROR = 400;
    private static final int CODE_INVALID_PARAM = 422;

    private static final String RESPONSE_HEAD_RETRY = "Retry-After";
    private static final String RESPONSE_HEAD_LIMIT = "X-RateLimit-Limit";
    private static final String RESPONSE_HEAD_REMAIN = "X-RateLimit-Remaining";
    private static final String RESPONSE_HEAD_RESET = "X-RateLimit-Reset";
    private static final String RESPONSE_HEAD_REDIRECT = "Location"; // 重定向

    private static final String RESPONSE_BODY_MESSAGE = "message";
    private static final String RESPONSE_BODY_TOTAL_COUNT = "total_count";
    private static final String RESPONSE_BODY_INCOMPLETE_RESULTS = "incomplete_results";
    private static final String RESPONSE_BODY_ITEMS = "items";
    private static final String RESPONSE_BODY_FILE_NAME = "name";
    private static final String RESPONSE_BODY_FILE_PATH = "path";
    private static final String RESPONSE_BODY_FILE_SHA = "sha";
    private static final String RESPONSE_BODY_FILE_HTML_URL = "html_url";
    private static final String RESPONSE_BODY_CONTENT_URL = "url";
    private static final String RESPONSE_BODY_REPO = "repository";
    private static final String RESPONSE_BODY_REPO_ID = "id";
    private static final String RESPONSE_BODY_REPO_NAME = "full_name";
    private static final String RESPONSE_BODY_REPO_HTML_URL = "html_url";
    private static final String RESPONSE_BODY_OWNER = "owner";
    private static final String RESPONSE_BODY_OWNER_ID = "id";
    private static final String RESPONSE_BODY_OWNER_NAME = "login";
    private static final String RESPONSE_BODY_OWNER_HTML_URL = "html_url";
    private static final String RESPONSE_BODY_CONTENT = "content";
    private static final String RESPONSE_BODY_MATCH_MATEDATA = "text_matches";
    private static final String RESPONSE_BODY_MATEDATA_OBJECT_URL = "object_url";
    private static final String RESPONSE_BODY_MATEDATA_FRAGMENT = "fragment";

    private static final Base64.Decoder decoder = Base64.getDecoder();

    private static String getStringHeader(Map<String, List<String>> responseHeaders, String key) {
        List<String> strings = responseHeaders.get(key);
        if (strings == null || strings.size() == 0) return null;
        String value = null;
        for (String val : strings) {
            if (val == null) continue;
            value = val;
            break;
        }
        return value;
    }

    private static Integer getIntHeader(Map<String, List<String>> responseHeaders, String key) {
        String value = getStringHeader(responseHeaders, key);
        return value != null ? Integer.valueOf(value) : null;
    }

    @SuppressWarnings("SameParameterValue")
    private static Long getLongHeader(Map<String, List<String>> responseHeaders, String key) {
        String value = getStringHeader(responseHeaders, key);
        return value != null ? Long.valueOf(value) : null;
    }

    @SuppressWarnings("CatchMayIgnoreException")
    private static void handleSearchStatusMessage(GithubSearchBaseVo baseVo, Integer responseCode, Map<String, List<String>> responseHeaders, String body) {
        if (responseCode == null) {
            baseVo.setStatus(GithubSearchBaseVo.Status.UNRESPONSIVE);
            return;
        }

        if (responseCode != CODE_SUCCESS) {
            LOG.debug("github api search failed, response code -> {}", responseCode);
            if (responseCode == CODE_PERMANENT_REDIRECTION) { // 永久重定向
                baseVo.setStatus(GithubSearchBaseVo.Status.PERMANENT_REDIRECTION);
                baseVo.setLocation(getStringHeader(responseHeaders, RESPONSE_HEAD_REDIRECT));
            } else if (responseCode == CODE_TEMPORARY_REDIRECTION_1 || responseCode == CODE_TEMPORARY_REDIRECTION_2) { // 临时重定向
                baseVo.setStatus(GithubSearchBaseVo.Status.TEMPORARY_REDIRECTION);
                baseVo.setLocation(getStringHeader(responseHeaders, RESPONSE_HEAD_REDIRECT));
            } else if (responseCode == CODE_JSON_ERROR || responseCode == CODE_INVALID_PARAM) {
                baseVo.setStatus(GithubSearchBaseVo.Status.CLIENT_ERROR);
                baseVo.setErrorMessage(getStringHeader(responseHeaders, RESPONSE_BODY_MESSAGE));
            } else {
                Integer retryAfter = getIntHeader(responseHeaders, RESPONSE_HEAD_RETRY);
                if (retryAfter != null) {
                    baseVo.setStatus(GithubSearchBaseVo.Status.RATE_ABUSED);
                    baseVo.setRetryAfter(retryAfter);
                } else {
                    baseVo.setStatus(GithubSearchBaseVo.Status.UNEXPECTED_ERROR);
                }
            }
            try {
                JSONObject jo = JSONObject.parseObject(body);
                if (jo != null && jo.getString(RESPONSE_BODY_MESSAGE) != null) {
                    baseVo.setErrorMessage(jo.getString(RESPONSE_BODY_MESSAGE));
                }
                LOG.debug("retryAfter -> " + baseVo.getRetryAfter() + "response message -> {}", baseVo.getErrorMessage());
            } catch (Exception e) {}
        } else {
            baseVo.setStatus(GithubSearchBaseVo.Status.SUCCESS);
            Integer limit = getIntHeader(responseHeaders, RESPONSE_HEAD_LIMIT);
            baseVo.setRateLimitLimit(limit != null ? limit : 30);
            Integer remain = getIntHeader(responseHeaders, RESPONSE_HEAD_REMAIN);
            baseVo.setRateLimitRemain(remain != null ? remain : 0);
            Long reset = getLongHeader(responseHeaders, RESPONSE_HEAD_RESET);
            baseVo.setRateLimitReset(reset != null ? reset : System.currentTimeMillis() + 10_000);
        }
    }

    private static Map<String, Object> requestHeaders(String token) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "token " + token);
        headers.put("User-Agent", USER_AGENT);
        return headers;
    }

    /**
     * github search code时，如果searchKey的值类似于www.xxx.com的字段，
     * 实际上github会以www xxx com三个值的方式搜索结果，因此会导致结果实际上不一定匹配的问题。
     * 这里要针对该问题重新验证一遍
     */
    private static void setMatedataAndTrueMatch(JSONObject json, GithubSearchCodeVo.ResultItem resultItem, String searchKey) {
        try {
            JSONArray ja = json.getJSONArray(RESPONSE_BODY_MATCH_MATEDATA);
            boolean match = false;
            for (int i = 0; i < ja.size(); i++) {
                try {
                    JSONObject jo = ja.getJSONObject(i);
                    jo.remove(RESPONSE_BODY_MATEDATA_OBJECT_URL);
                    if (!match) {
                        String fragment = jo.getString(RESPONSE_BODY_MATEDATA_FRAGMENT);
                        match = fragment.contains(searchKey);
                    }
                } catch (Exception ignored){}
            }
            resultItem.setMatchMetadata(ja.toString());
            resultItem.setTrueMatch(match);
        } catch (Exception ignored) {
        }
    }

    private static void readCodeList(GithubSearchCodeVo searchCodeVo, String responseBody) {
        try {
            if (searchCodeVo.getStatus() != GithubSearchBaseVo.Status.SUCCESS) return;
            JSONObject jo = JSONObject.parseObject(responseBody);
            if (jo == null) return;
            JSONArray items = jo.getJSONArray(RESPONSE_BODY_ITEMS);
            if (items == null) return;

            Long totalCount = jo.getLong(RESPONSE_BODY_TOTAL_COUNT);
            searchCodeVo.setTotalCount(totalCount);
            searchCodeVo.setIncompleteResults(jo.getBoolean(RESPONSE_BODY_INCOMPLETE_RESULTS));

            Integer currentPageNo = searchCodeVo.getCurrentPageNo();
            Integer currentPageSize = searchCodeVo.getCurrentPageSize();
            int startIndex = (currentPageNo - 1) * currentPageSize;

            List<GithubSearchCodeVo.ResultItem> list = new ArrayList<>();

            for (int i = 0; i < items.size(); i++) {
                JSONObject joi = items.getJSONObject(i);
                GithubSearchCodeVo.ResultItem item = new GithubSearchCodeVo.ResultItem();
                item.setFileName(joi.getString(RESPONSE_BODY_FILE_NAME));
                item.setFilePath(joi.getString(RESPONSE_BODY_FILE_PATH));
                item.setFileSha(joi.getString(RESPONSE_BODY_FILE_SHA));
                item.setContentUrl(joi.getString(RESPONSE_BODY_CONTENT_URL));
                item.setFileHtmlUrl(joi.getString(RESPONSE_BODY_FILE_HTML_URL));
                JSONObject repoJo = joi.getJSONObject(RESPONSE_BODY_REPO);
                item.setRepoId(repoJo.getLong(RESPONSE_BODY_REPO_ID));
                item.setRepoFullName(repoJo.getString(RESPONSE_BODY_REPO_NAME));
                item.setRepoHtmlUrl(repoJo.getString(RESPONSE_BODY_REPO_HTML_URL));
                JSONObject ownerJo = repoJo.getJSONObject(RESPONSE_BODY_OWNER);
                item.setOwerId(ownerJo.getLong(RESPONSE_BODY_OWNER_ID));
                item.setOwnerName(ownerJo.getString(RESPONSE_BODY_OWNER_NAME));
                item.setOwnerHtmlUrl(ownerJo.getString(RESPONSE_BODY_OWNER_HTML_URL));
                setMatedataAndTrueMatch(joi, item, searchCodeVo.getSearchKey());

                long currentIndex = startIndex + i + 1;
                item.setIndex(currentIndex);

                list.add(item);
            }
            searchCodeVo.setItems(list);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0 && stackTrace[0].getClassName().contains("com.alibaba.fastjson.JSON")) {
                LOG.debug(responseBody);
            }
        }
    }

    /**
     * 查询code列表入口
     * @param keywords 关键字
     * @param scopes 附加的筛选范围（如：指定编程语言）
     * @param page 第几页
     * @param perPage 每页数量
     * @param token GitHub token
     */
    private static GithubSearchCodeVo searchCodeList(String keywords, String scopes, int page, int perPage, String token) {
        Map<String, Object> headers = requestHeaders(token);
        headers.put("Accept", ACCEPT_TEXT_MATCH_MATEDATA);

        Map<String, Object> params = new HashMap<>();
        scopes = scopes == null ? "" : scopes;
        params.put("q", keywords + " " + scopes);
        params.put("per_page", perPage);
        params.put("page", page);

        GithubSearchCodeVo searchVo = new GithubSearchCodeVo();
        searchVo.setSearchKey(keywords);
        searchVo.setScope(scopes);
        try {
            HttpClientUtil.HttpResponseVo httpResponseVo = HttpClientUtil.doGet(GITHUB_API_SEARCH_CODE, headers, params);
            Integer responseCode = httpResponseVo.getCode();
            Map<String, List<String>> responseHeaders = httpResponseVo.getHeaders();
            String responseBody = httpResponseVo.getBody();

            handleSearchStatusMessage(searchVo, responseCode, responseHeaders, responseBody);

            searchVo.setCurrentPageNo(page);
            searchVo.setCurrentPageSize(perPage);
            readCodeList(searchVo, responseBody);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return searchVo;
    }

    @SuppressWarnings("unused")
    public static GithubSearchCodeVo searchCodeList(String keywords, String scopes, int page, int perPage, GithubTokenManager tokenManager) {
        GithubTokenManager.GithubTokenRenter tokenRenter = null;
        GithubSearchCodeVo vo = null;
        try {
            tokenRenter = tokenManager.tryGetAvailableToken(GET_TOKEN_DELAY, GithubTokenManager.RentType.SEARCH_CODE);
            if (tokenRenter == null) {
                return null;
            }
            GithubToken token = tokenRenter.getToken();
            vo = searchCodeList(keywords, scopes, page, perPage, token.getToken());
            return vo;
        } finally {
            if (tokenRenter != null) {
                tokenRenter.giveBackToken(vo != null ? vo.getRateLimitRemain() : Integer.valueOf(1),
                        vo != null ? vo.getRateLimitReset() : Long.valueOf(0), vo != null ? vo.getRetryAfter() : null);
            }
        }
    }

    @SuppressWarnings("CatchMayIgnoreException")
    private static String base64Decode(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder();
        String[] split = str.split("\n");
        Arrays.stream(split).forEach(s -> {
            sb.append("\n");
            try {
                byte[] decode = decoder.decode(s);
                sb.append(new String(decode, CHARSET));
            } catch (Exception e) {
            }
        });
        if (sb.length() > 0) {
            sb.delete(0, 1);
        }
        return sb.toString();
    }

    private static void readContent(GithubSearchContentVo contentVo, String responseBody) {
        try {
            JSONObject jo = JSONObject.parseObject(responseBody);
            if (jo == null) {
                return;
            }
            contentVo.setContentBase64(jo.getString(RESPONSE_BODY_CONTENT));
            if (contentVo.getContentBase64() == null) return;
            contentVo.setContent(base64Decode(contentVo.getContentBase64()));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0 && stackTrace[0].getClassName().contains("com.alibaba.fastjson.JSON")) {
                LOG.info(responseBody);
            }
        }
    }

    private static GithubSearchContentVo searchContent(String url, String token) {
        Map<String, Object> headers = requestHeaders(token);

        GithubSearchContentVo searchVo = new GithubSearchContentVo();
        try {
            HttpClientUtil.HttpResponseVo httpResponseVo = HttpClientUtil.doGet(url, headers, null);
            Integer responseCode = httpResponseVo.getCode();
            Map<String, List<String>> responseHeaders = httpResponseVo.getHeaders();
            String responseBody = httpResponseVo.getBody();

            handleSearchStatusMessage(searchVo, responseCode, responseHeaders, responseBody);

            readContent(searchVo, responseBody);

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return searchVo;
    }

    @SuppressWarnings("unused")
    public static GithubSearchContentVo searchContent(String url, GithubTokenManager tokenManager) {
        GithubTokenManager.GithubTokenRenter tokenRenter = null;
        GithubSearchContentVo vo = null;
        try {
            tokenRenter = tokenManager.tryGetAvailableToken(4000L, GithubTokenManager.RentType.SEARCH_CONETNT);
            if (tokenRenter == null) {
                return null;
            }
            GithubToken token = tokenRenter.getToken();
            vo = searchContent(url, token.getToken());
            return vo;
        } finally {
            if (tokenRenter != null) {
                tokenRenter.giveBackToken(vo != null ? vo.getRateLimitRemain() : Integer.valueOf(1),
                        vo != null ? vo.getRateLimitReset() : Long.valueOf(0), vo != null ? vo.getRetryAfter() : null);
            }
        }
    }

    public static void main(String[] args) throws IOException {

        GithubSearchCodeVo vo = GithubApi.searchCodeList("rec", "",
                1, 2, "a27db789e1d24cf531cc3d034754eea3bd3860e7");

        List<GithubSearchCodeVo.ResultItem> items = vo.getItems();
        if (items == null || items.size() == 0) {
            System.out.println("items => " + items);
            return;
        }

        System.out.println("content rs => ");
        GithubSearchCodeVo.ResultItem resultItem = items.get(0);
        String contentUrl = resultItem.getContentUrl();
        GithubSearchContentVo contentVo = GithubApi.searchContent(contentUrl, "a27db789e1d24cf531cc3d034754eea3bd3860e7");
        
    }
}
