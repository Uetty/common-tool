package com.uetty.common.tool.core.word;

import lombok.Data;

/**
 * @author vince
 */
@Data
public class WordTableCell {

    /**
     * 跨行合并单元格
     */
    private boolean rowMerge = false;
    /**
     * 跨行合并单元格时位于第几行
     */
    private int rowMergeIndex = 0;
    /**
     * 跨列合并单元格
     */
    private boolean colMerge = false;
    /**
     * 跨列合并单元格位于第几列
     */
    private int colMergeIndex = 0;
    /**
     * 跨列合并单元格时，如果当前是第0列，需要记录合并的总列数
     */
    private int colMergeSize = 1;
    /**
     * 文本内容
     */
    private String cellValue;
//    /**
//     * 带样式的文本内容
//     */
//    private StylizedMultilineString styledValue;

}
