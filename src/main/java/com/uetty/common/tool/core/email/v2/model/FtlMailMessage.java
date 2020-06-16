package com.uetty.common.tool.core.email.v2.model;

import com.uetty.common.tool.core.email.v2.ftl.FtlEnum;

import java.util.Map;

/**
 * @author : Vince
 * @date: 2019/8/2 14:00
 */
public class FtlMailMessage extends MailMessage {

    // Object 一般为String, File，如果为File则会自动处理为inline文件
    private Map<String, Object> dataModel;
    private FtlEnum ftlEnum;

    public Map<String, Object> getDataModel() {
        return dataModel;
    }

    public void setDataModel(Map<String, Object> dataModel) {
        this.dataModel = dataModel;
    }

    public FtlEnum getFtlEnum() {
        return ftlEnum;
    }

    public void setFtlEnum(FtlEnum ftlEnum) {
        this.ftlEnum = ftlEnum;
    }
}
