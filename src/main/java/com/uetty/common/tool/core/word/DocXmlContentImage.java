package com.uetty.common.tool.core.word;

/**
 * doc文件xml模板辅助html img标签显示的对象
 * @author Vince
 * @date 2019/11/22 14:37
 */
public class DocXmlContentImage extends DocXmlContent {

    /**
     * v:shape中style:width使用
     */
    private Double width;
    /**
     * v:shape中style:height使用
     */
    private Double height;
    /**
     * Relationship中Target，pkg:part中contentType 使用
     */
    private String name;
    /**
     * Relationship中的Id 使用
     */
    private String relationId;
    /**
     * v:shape中的id使用
     */
    private String sharpId;
    /**
     * 文件类型，pkg:part中name使用
     */
    private String fileType;
    /**
     * base64编码的data，pkg:part中使用
     */
    private String base64;
    /**
     * 附件编号
     */
    private String appendixNo;
    /**
     * 展示名
     */
    private String displayName;

    public DocXmlContentImage() {
        setType(DocXmlContent.CONTENT_TYPE_IMG);
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public String getSharpId() {
        return sharpId;
    }

    public void setSharpId(String sharpId) {
        this.sharpId = sharpId;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getAppendixNo() {
        return appendixNo;
    }

    public void setAppendixNo(String appendixNo) {
        this.appendixNo = appendixNo;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
