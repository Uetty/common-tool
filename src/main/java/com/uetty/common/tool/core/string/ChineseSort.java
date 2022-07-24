package com.uetty.common.tool.core.string;

import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 中文排序
 * @author vince
 */
public class ChineseSort {

    static final int CHINESE_START = 0x4e00;

    static final int CHINESE_END = 0x9fa5;

    static final Collator CHINESE_COLLATOR = Collator.getInstance(Locale.CHINA);

    static final int CHINESE_WIDTH = CHINESE_END - CHINESE_START + 1;

    /**
     * 中文字符先于非中文字符，中文字符之间按unicode码点比较，非中文字符之间按unicode码点比较
     */
    public static int chineseFirstCompare(String chars1, String chars2) {
        return chineseFirstCompare(chars1.toCharArray(), chars2.toCharArray());
    }

    /**
     * 中文字符先于非中文字符，中文字符之间按unicode码点比较，非中文字符之间按unicode码点比较
     */
    public static int chineseFirstCompare(char[] chars1, char[] chars2) {
        for (int i = 0; i < chars1.length && i < chars2.length; i++) {
            int h1 = chineseFirstHash(chars1[i]);
            int h2 = chineseFirstHash(chars2[i]);

            if (h1 != h2) {
                return h1 - h2;
            }
        }
        return chars1.length - chars2.length;
    }

    /**
     * 中文字符先于非中文字符，中文字符之间按拼音比较，非中文字符之间按unicode码点比较
     */
    public static int chineseFirstPinyinCompare(String chars1, String chars2) {
        return chineseFirstPinyinCompare(chars1.toCharArray(), chars2.toCharArray());
    }

    /**
     * 中文字符先于非中文字符，中文字符之间按拼音比较，非中文字符之间按unicode码点比较
     */
    public static int pinyinIntialCompare(String chars1, String chars2) {
        return pinyinIntialCompare(chars1.toCharArray(), chars2.toCharArray());
    }

    /**
     * 中文字符先于非中文字符，中文字符之间按拼音比较，非中文字符之间按unicode码点比较
     */
    public static int chineseFirstPinyinCompare(char[] chars1, char[] chars2) {
        for (int i = 0; i < chars1.length && i < chars2.length; i++) {

            int compareTo = chineseFirstAndPinyinCompare(chars1[i], chars2[i]);

            if (compareTo != 0) {
                return compareTo;
            }
        }
        return chars1.length - chars2.length;
    }

    /**
     * 按拼音首字母比较，如果相同按unicode码点比较
     */
    public static int pinyinIntialCompare(char[] chars1, char[] chars2) {
        for (int i = 0; i < chars1.length && i < chars2.length; i++) {

            int compareTo = pinyinIntialCompare(chars1[i], chars2[i]);

            if (compareTo != 0) {
                return compareTo;
            }
        }
        return chars1.length - chars2.length;
    }

    /**
     * 按拼音首字母比较（英文与数字同样转为首字母），如果首字母相同按unicode码点比较
     */
    private static int pinyinIntialCompare(char c1, char c2) {
        if (c1 == c2) {
            return 0;
        }

        char i1 = PinYinInitialTool.char2Initial(c1);
        char i2 = PinYinInitialTool.char2Initial(c2);

        if (i1 != i2) {
            return i1 - i2;
        }
        return c1 - c2;
    }

    private static int chineseFirstAndPinyinCompare(char c1, char c2) {
        if (c1 == c2) {
            return 0;
        }

        int h1 = chineseFirstHash(c1);
        int h2 = chineseFirstHash(c2);

        if (h1 < CHINESE_WIDTH && h2 < CHINESE_WIDTH) {
            return CHINESE_COLLATOR.compare("" + c1, "" + c2);
        }

        return h1 - h2;
    }

    private static int chineseFirstHash(char c) {
        if (c >= CHINESE_START && c <= CHINESE_END) {
            return c - CHINESE_START;
        }
        if (c < CHINESE_START) {
            return c + CHINESE_WIDTH;
        }
        return c;
    }

}
