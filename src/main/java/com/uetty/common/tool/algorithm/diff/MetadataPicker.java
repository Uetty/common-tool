package com.uetty.common.tool.algorithm.diff;

/**
 * 元数据（不可分割）采拣器
 * <p>不同采拣方式会影响内存的占用率，采拣完的数据数组越小，diff时内存消耗越小</p>
 * @author : Vince
 */
public interface MetadataPicker {
    Metadatas doPick(String str);
}