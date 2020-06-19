package com.uetty.common.tool.core.leetcode.strval;

@SuppressWarnings("unused")
public class TreeNode {
    public String val;
    public TreeNode left;
    public TreeNode right;
    public TreeNode() {}
    public TreeNode(String val) { this.val = val; }
    public TreeNode(String val, TreeNode left, TreeNode right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}
