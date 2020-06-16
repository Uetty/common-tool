package com.uetty.common.tool.algorithm.diff;

/**
 * 采拣单个字符作为一个不可分割的数据元
 * <p>每个字符作为一个数据元，内存消耗极大，适合短文本使用(3000字符数以下，具体数值与机器内存相关)</p>
 * @author : Vince
 */
public class CharactMetadataPicker implements MetadataPicker {
    @Override
    public Metadatas doPick(String str) {
        Metadatas metas = new Metadatas();
        metas.setData(new String[str.length()]);
        for (int i = 0; i < str.length(); i++) {
            metas.getData()[i] = str.substring(i, i + 1);
        }
        return metas;
    }
}