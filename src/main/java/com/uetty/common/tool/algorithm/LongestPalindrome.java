package com.uetty.common.tool.algorithm;

/**
 * 最长回文串 Manacher法
 * @author Vince
 */
@SuppressWarnings("unused")
public class LongestPalindrome {

    public String manacher(String s) {
        if (s == null || s.length() < 2) return s;
        final char ichar = '%';
        char[] chars = new char[s.length() * 2 + 1];
        chars[0] = ichar;
        for (int i = 0; i < s.length(); i++) {
            chars[2 * i + 1] = s.charAt(i);
            chars[2 * i + 2] = ichar;
        }

        int[] lens = new int[chars.length];
        lens[0] = 0;
        lens[1] = 1;

        int maxRightP = 1;
        int maxLenP = 1;
        for (int i = 2; i + lens[maxLenP] < chars.length; i++) {
            if (i < maxRightP + lens[maxRightP]) {
                // 复用对称部分的是否回文串结果
                if (lens[2 * maxRightP - i] < maxRightP + lens[maxRightP] - i) {
                    lens[i] = lens[2 * maxRightP - i];
                    continue;
                } else {
                    lens[i] = maxRightP + lens[maxRightP] - i;
                }
            }
            while (i + lens[i] + 1 < chars.length && i - lens[i] - 1 >= 0) {
                if (chars[i + lens[i] + 1] != chars[i - lens[i] - 1]) {
                    break;
                }
                lens[i]++;
            }
            if (lens[i] > lens[maxLenP]) {
                maxLenP = i;
            }
            if (i + lens[i] >= maxRightP + lens[maxRightP]) {
                maxRightP = i;
            }
        }
        char[] rchars = new char[lens[maxLenP]];
        int start = maxLenP - lens[maxLenP];
        for (int i = 0; i < rchars.length; i++) {
            rchars[i] = chars[start + 2 * i + 1];
        }
        return new String(rchars);
    }
}
