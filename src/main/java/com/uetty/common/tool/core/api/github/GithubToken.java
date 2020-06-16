package com.uetty.common.tool.core.api.github;

/**
 * 
 */
public class GithubToken {

    private Integer id;
    private String token;
    private String author;
    private String description;


    private Integer currentRateLimitLimit = 30; // 从请求响应头中更新到的值：限制次数
    private Integer currentRateLimitRemain = 30; // 从请求响应头中更新到的值：重置时间之前的剩余次数
    private Long currentRateLimitReset = 0L; // 从请求响应头中更新到的值：重置时间
    private Boolean currentRateLimitAbused = false; // 从请求响应结果得到的反馈：是否被滥用禁止
    private Integer retryAfter = 0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCurrentRateLimitLimit() {
        return currentRateLimitLimit;
    }

    public void setCurrentRateLimitLimit(Integer currentRateLimitLimit) {
        this.currentRateLimitLimit = currentRateLimitLimit;
    }

    public Integer getCurrentRateLimitRemain() {
        return currentRateLimitRemain;
    }

    public void setCurrentRateLimitRemain(Integer currentRateLimitRemain) {
        this.currentRateLimitRemain = currentRateLimitRemain;
    }

    public Long getCurrentRateLimitReset() {
        return currentRateLimitReset == null ? 0L : currentRateLimitReset;
    }

    public void setCurrentRateLimitReset(Long currentRateLimitReset) {
        this.currentRateLimitReset = currentRateLimitReset;
    }

    public Boolean getCurrentRateLimitAbused() {
        return currentRateLimitAbused == null ? false : currentRateLimitAbused;
    }

    public void setCurrentRateLimitAbused(Boolean currentRateLimitAbused) {
        this.currentRateLimitAbused = currentRateLimitAbused;
    }

    public Integer getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(Integer retryAfter) {
        this.retryAfter = retryAfter;
    }
}
