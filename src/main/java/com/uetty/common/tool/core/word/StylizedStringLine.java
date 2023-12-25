package com.uetty.common.tool.core.word;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 带样式描述的字符串
 * @author vince
 */
@Builder
@Data
public class StylizedStringLine {

    /**
     * 是否列表样式
     */
    private boolean isListStyle;

    /**
     * 在列表中的次序
     */
    private Integer listIndex;

    /**
     * 带样式内容
     */
    @Builder.Default
    private List<StylizedString> contents = new ArrayList<>();
}
