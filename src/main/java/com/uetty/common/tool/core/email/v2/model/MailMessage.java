package com.uetty.common.tool.core.email.v2.model;

import java.io.File;
import java.util.List;

/**
 * @author : Vince
 */
@SuppressWarnings("unused")
public class MailMessage {

    private String fromName;
    private String title;
    private List<String> toUsers;
    private List<String> ccUsers;
    private List<String> bccUsers;
    // Object 一般为String, File，如果为File则会自动处理为inline内嵌文件形式，如图片
    private List<File> fileAttachments; // 文件附件
    private String content;
    private boolean htmlType = true;

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getToUsers() {
        return toUsers;
    }

    public void setToUsers(List<String> toUsers) {
        this.toUsers = toUsers;
    }

    public List<String> getCcUsers() {
        return ccUsers;
    }

    public void setCcUsers(List<String> ccUsers) {
        this.ccUsers = ccUsers;
    }

    public List<String> getBccUsers() {
        return bccUsers;
    }

    public void setBccUsers(List<String> bccUsers) {
        this.bccUsers = bccUsers;
    }

    public List<File> getFileAttachments() {
        return fileAttachments;
    }

    public void setFileAttachments(List<File> fileAttachments) {
        this.fileAttachments = fileAttachments;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getHtmlType() {
        return htmlType;
    }

    public void setHtmlType(boolean htmlType) {
        this.htmlType = htmlType;
    }
}
