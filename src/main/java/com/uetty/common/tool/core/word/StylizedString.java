package com.uetty.common.tool.core.word;

import lombok.Builder;
import lombok.Data;

/**
 * 带样式描述的字符串
 * @author vince
 */
@Builder
@Data
public class StylizedString {

    /**
     * 文本内容
     */
    private String text;

    /**
     * 是否加粗
     */
    private boolean isBold;

    /**
     * 文字大小，为空时使用默认值
     */
    private Integer size;
}
