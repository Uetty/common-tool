package com.uetty.common.tool.core.email.v2.model;

import java.io.File;
import java.net.URI;

/**
 * 某些特殊需求可能需要输出的文件名与本地存储的文件名不同
 * @author : Vince
 * @date: 2019/8/7 17:19
 */
public class MailFile extends File {

    String fileName;

    public MailFile(String pathname) {
        super(pathname);
    }
    public MailFile(String parent, String child) {
        super(parent, child);
    }
    public MailFile(File parent, String child) {
        super(parent, child);
    }
    public MailFile(URI uri) {
        super(uri);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        if (fileName != null) {
            return fileName;
        }
        return super.getName();
    }
}
