package com.uetty.common.tool.core.api.github;

public class GithubSearchContentVo extends GithubSearchBaseVo {

    private String content;
    private String contentBase64;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentBase64() {
        return contentBase64;
    }

    public void setContentBase64(String contentBase64) {
        this.contentBase64 = contentBase64;
    }
}
