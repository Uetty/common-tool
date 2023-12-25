package com.uetty.common.tool.core.word;

/**
 * @author Vince
 * @date 2019/11/22 14:34
 */
public class DocXmlContent {

    public static final String CONTENT_TYPE_TEXT = "text";
    public static final String CONTENT_TYPE_IMG = "image";
    public static final String CONTENT_TYPE_FILE = "file"
            ;

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
