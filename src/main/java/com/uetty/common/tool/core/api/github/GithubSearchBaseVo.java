package com.uetty.common.tool.core.api.github;

/**
 * Github Search Api 的公共返回值封装
 */
@SuppressWarnings("unused")
public class GithubSearchBaseVo {

    public enum Status {
        /** 成功 */ SUCCESS,
        /** 未响应 */ UNRESPONSIVE,
        /** 频率滥用限制 */ RATE_ABUSED,
        /** 永久重定向（代码需要修改） */ PERMANENT_REDIRECTION,
        /** 临时重定向 */ TEMPORARY_REDIRECTION,
        /** 客户端错误（代码需要检查） */ CLIENT_ERROR,
        /** 未知错误 */ UNEXPECTED_ERROR,
    }

    private Status status; // 状态
    private String errorMessage; // 错误信息
    private Integer rateLimitLimit; // 响应头中得到的请求限制 X-RateLimit-Limit
    private Integer rateLimitRemain; // 响应头中得到的剩余请求数 X-RateLimit-Remaining
    private Long rateLimitReset; // 响应头中得到的重置时间，毫秒，需要将秒转为毫秒后存储 X-RateLimit-Reset
    private Integer retryAfter;
    private String location; // 重定向路径

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRateLimitLimit() {
        return rateLimitLimit;
    }

    public void setRateLimitLimit(Integer rateLimitLimit) {
        this.rateLimitLimit = rateLimitLimit;
    }

    public Integer getRateLimitRemain() {
        return rateLimitRemain;
    }

    public void setRateLimitRemain(Integer rateLimitRemain) {
        this.rateLimitRemain = rateLimitRemain;
    }

    public Long getRateLimitReset() {
        return rateLimitReset;
    }

    public void setRateLimitReset(Long rateLimitReset) {
        this.rateLimitReset = rateLimitReset;
    }

    public Integer getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(Integer retryAfter) {
        this.retryAfter = retryAfter;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
