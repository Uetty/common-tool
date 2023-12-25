package com.uetty.common.tool.core.word;

import com.uetty.common.tool.core.string.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 样式化多行文本字符串
 * @author vince
 */
public class StylizedMultilineString {

    private final List<StylizedStringLine> lines;

    public StylizedMultilineString() {
        lines = new ArrayList<>();
    }

    public List<StylizedStringLine> getLines() {
        return lines;
    }

    /**
     * 添加新行
     */
    public void addLine() {
        lines.add(
                StylizedStringLine.builder().build()
        );
    }

    /**
     * 添加列表样式的新行
     */
    public void addListLine() {
        int listIndex = 0;

        if (lines.size() > 0) {
            StylizedStringLine stylizedStringLine = lines.get(lines.size() - 1);
            if (stylizedStringLine.isListStyle()) {
                listIndex = stylizedStringLine.getListIndex() + 1;
            }
        }

        lines.add(
                StylizedStringLine.builder()
                        .isListStyle(true)
                        .listIndex(listIndex + 1)
                        .build()
        );
    }

    public int lineSize() {
        return lines.size();
    }

    public void addText(String text) {
        if (lines.size() == 0) {
            throw new RuntimeException("no lines exists, please add line first");
        }
        StylizedStringLine stylizedStringLine = lines.get(lines.size() - 1);
        List<StylizedString> contents = stylizedStringLine.getContents();
        StylizedString stylizedString = StylizedString.builder()
                .text(StringUtil.def(text, ""))
                .build();
        contents.add(stylizedString);
    }

    public void addText(String text, int size) {
        if (lines.size() == 0) {
            throw new RuntimeException("no lines exists, please add line first");
        }
        StylizedStringLine stylizedStringLine = lines.get(lines.size() - 1);
        List<StylizedString> contents = stylizedStringLine.getContents();
        StylizedString stylizedString = StylizedString.builder()
                .text(StringUtil.def(text, ""))
                .size(size)
                .build();
        contents.add(stylizedString);
    }

    public void addBoldText(String text) {
        if (lines.size() == 0) {
            throw new RuntimeException("no lines exists, please add line first");
        }
        StylizedStringLine stylizedStringLine = lines.get(lines.size() - 1);
        List<StylizedString> contents = stylizedStringLine.getContents();
        StylizedString stylizedString = StylizedString.builder()
                .text(StringUtil.def(text, ""))
                .isBold(true)
                .build();
        contents.add(stylizedString);
    }

    public void addBoldText(String text, int size) {
        if (lines.size() == 0) {
            throw new RuntimeException("no lines exists, please add line first");
        }
        StylizedStringLine stylizedStringLine = lines.get(lines.size() - 1);
        List<StylizedString> contents = stylizedStringLine.getContents();
        StylizedString stylizedString = StylizedString.builder()
                .text(StringUtil.def(text, ""))
                .isBold(true)
                .size(size)
                .build();
        contents.add(stylizedString);
    }

    public void addLines(StylizedMultilineString multilineString) {
        this.lines.addAll(multilineString.lines);
    }


}
