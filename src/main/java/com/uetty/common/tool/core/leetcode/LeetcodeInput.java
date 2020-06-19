package com.uetty.common.tool.core.leetcode;

import com.uetty.common.tool.core.leetcode.strval.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * 用于leetcode编程时debug
 */
@SuppressWarnings("unused")
public class LeetcodeInput {

    private final Scanner scanner = new Scanner(System.in);

    public com.uetty.common.tool.core.leetcode.intval.TreeNode buildIntTreeNode(int[] arr) {
        return buildIntTreeNodeByArray(arr, 0);
    }

    public com.uetty.common.tool.core.leetcode.intval.TreeNode buildIntegerTreeNode(Integer[] array) {
        return buildIntTreeNodeByArray2(array, 0);
    }

    public TreeNode buildStringTreeNode(String[] array) {
        return buildStringTreeNodeByArray(array, 0);
    }

    public com.uetty.common.tool.core.leetcode.intval.TreeNode buildIntTreeNodeByArray(int[] array, int index) {
        com.uetty.common.tool.core.leetcode.intval.TreeNode tn = null;
        if (index < array.length) {
            int value = array[index];
            tn = new com.uetty.common.tool.core.leetcode.intval.TreeNode(value);
            tn.left = buildIntTreeNodeByArray(array, 2 * index + 1);
            tn.right = buildIntTreeNodeByArray(array, 2 * index + 2);
        }
        return tn;
    }

    public TreeNode buildStringTreeNodeByArray(String[] array, int index) {
        TreeNode tn = null;
        if (index < array.length && array[index] != null) {
            tn = new TreeNode(array[index]);
            tn.left = buildStringTreeNodeByArray(array, 2 * index + 1);
            tn.right = buildStringTreeNodeByArray(array, 2 * index + 2);
        }
        return tn;
    }

    public com.uetty.common.tool.core.leetcode.intval.TreeNode buildIntTreeNodeByArray2(Integer[] array, int index) {
        com.uetty.common.tool.core.leetcode.intval.TreeNode tn = null;
        if (index < array.length && array[index] != null) {
            tn = new com.uetty.common.tool.core.leetcode.intval.TreeNode(array[index]);
            tn.left = buildIntTreeNodeByArray2(array, 2 * index + 1);
            tn.right = buildIntTreeNodeByArray2(array, 2 * index + 2);
        }
        return tn;
    }

    public Integer[] readIntegerArray() {
        List<Integer> list = new ArrayList<>();

        String next = scanner.next().trim();
        if (next.startsWith("[")) {
            next = next.substring(1);
        }
        if (next.endsWith("]")) {
            next = next.substring(0, next.length() - 1);
        }
        String[] split = next.split(",");
        for (String str : split) {
            if ("null".equals(str.trim())) {
                list.add(null);
            } else {
                list.add(Integer.parseInt(str.trim()));
            }
        }
        return list.toArray(new Integer[0]);
    }

    public com.uetty.common.tool.core.leetcode.intval.TreeNode readIntegerTreeNode() {
        Integer[] integers = readIntegerArray();
        return buildIntegerTreeNode(integers);
    }

    public TreeNode readStringTreeNode() {
        String[] strings = readStringArray();
        return buildStringTreeNode(strings);
    }

    public String readString() {
        String trim = scanner.next().trim();
        if (trim.startsWith("\"")) {
            trim = trim.substring(1);
        }
        if (trim.endsWith("\"")) {
            trim = trim.substring(0, trim.length() - 1);
        }
        return trim;
    }

    public int readInt() {
        String trim = scanner.next().trim();
        if (trim.startsWith("\"")) {
            trim = trim.substring(1);
        }
        if (trim.endsWith("\"")) {
            trim = trim.substring(0, trim.length() - 1);
        }
        return Integer.parseInt(trim);
    }

    public List<String> readStringList() {
        List<String> list = new ArrayList<>();
        String next = scanner.next().trim();
        if (next.startsWith("[")) {
            next = next.substring(1);
        }
        if (next.endsWith("]")) {
            next = next.substring(0, next.length() - 1);
        }
        String[] split = next.split(",");
        for (String str : split) {
            if (Objects.equals(str.trim(), "null")) {
                list.add(null);
                continue;
            }
            String trim = str.trim();
            if (trim.startsWith("\"")) {
                trim = trim.substring(1);
            }
            if (trim.endsWith("\"")) {
                trim = trim.substring(0, trim.length() - 1);
            }
            list.add(trim);
        }
        return list;
    }

    public String[] readStringArray() {
        return readStringList().toArray(new String[0]);
    }

    public int[] readIntArray() {
        List<Integer> list = new ArrayList<>();
        String next = scanner.next().trim();
        if (next.startsWith("[")) {
            next = next.substring(1);
        }
        if (next.endsWith("]")) {
            next = next.substring(0, next.length() - 1);
        }
        String[] split = next.split(",");
        for (String str : split) {
            list.add(Integer.parseInt(str.trim()));
        }
        return list.stream().mapToInt(Integer::intValue).toArray();
    }

    public int[][] readIntArray2() {
        List<List<Integer>> list = readIntList2();
        int[][] arrs = new int[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            List<Integer> item = list.get(i);
            arrs[i] = item.stream().mapToInt(Integer::intValue).toArray();
        }
        return arrs;
    }

    public List<List<Integer>> readIntList2() {
        List<List<Integer>> list = new ArrayList<>();
        String next = scanner.next().trim();
        if (next.startsWith("[")) {
            next = next.substring(1);
        }
        if (next.endsWith("]")) {
            next = next.substring(0, next.length() - 1);
        }
        String[] split = next.split(",");
        List<Integer> group = new ArrayList<>();
        for (String str : split) {
            str = str.trim();
            if (str.startsWith("[")) {
                str = str.substring(1);
                group = new ArrayList<>();
                list.add(group);
            }
            if (str.endsWith("]")) {
                str = str.substring(0, str.length() - 1);
            }

            String[] split1 = str.split(",");
            for (String str1 : split1) {
                group.add(Integer.parseInt(str1.trim()));
            }
        }
        return list;
    }
}
