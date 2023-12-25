package com.uetty.common.tool.core.word;

/**
 * @author Vince
 * @date 2019/11/22 14:35
 */
public class DocXmlContentText extends DocXmlContent {

    private String text;

    public DocXmlContentText() {
        setType(DocXmlContent.CONTENT_TYPE_TEXT);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
