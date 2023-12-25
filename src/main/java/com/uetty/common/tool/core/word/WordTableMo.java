package com.uetty.common.tool.core.word;

import com.uetty.common.tool.core.string.StringUtil;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author vince
 */
@Getter
public class WordTableMo {

    private int headRowSize = 1;

    private int headColSize = 10;

    private WordTableCell[][] headCells;

    private WordTableCell[][] bodyCells;

    private String[][] bodyDatas;

    private boolean allowBodyColMerge;

    private boolean allowBodyRowMerge;

    public WordTableMo() {
    }

    /**
     * 初始化表格单元格（优先跨列合并单元格）
     * @param cellValues 表格单元格文本
     * @param allowColMerge 是否允许跨列合并单元格
     * @param allowRowMerge 是否允许跨行合并单元格
     */
    private WordTableCell[][] initCells(String[][] cellValues, boolean allowColMerge, boolean allowRowMerge) {
        WordTableCell[][] cellMatrix = new WordTableCell[cellValues.length][cellValues[0].length];
        // 以跨列合并单元格优先，先进行一次数据初始化，初始化单元格文本的同时初始化计算跨列合并单元格情况
        for (int i = 0; i < cellValues.length; i++) {
            WordTableCell[] rowCells = new WordTableCell[cellValues[0].length];
            cellMatrix[i] = rowCells;
            for (int j = 0; j < cellValues[i].length; j++) {
                rowCells[j] = new WordTableCell();
                rowCells[j].setCellValue(cellValues[i][j]);

                // 计算跨列合并单元格
                if (allowColMerge && j > 0) {
                    // 第二列开始，需要计算是否跨列合并单元格
                    int preCellIndex = j - 1;
                    if (rowCells[preCellIndex].isColMerge()) {
                        // 上个单元格是跨列合并单元格，取合并块的第一个单元格
                        preCellIndex = j - 1 - rowCells[preCellIndex].getColMergeIndex();
                    }
                    // 判断是否要跨列合并单元格
                    if (StringUtil.equalsIgcNbsX(cellValues[i][preCellIndex], cellValues[i][j])) {
                        // 合并单元格
                        rowCells[j].setColMerge(true);
                        // 更新当前单元格位于合并单元格第几列
                        rowCells[j].setColMergeIndex(j - preCellIndex);
                        rowCells[preCellIndex].setColMerge(true);
                        // 更新跨列合并单元格总共列数
                        rowCells[preCellIndex].setColMergeSize(j - preCellIndex + 1);
                    }
                }
            }
        }

        // 计算跨行合并单元格
        if (allowRowMerge) {
            // 计算跨行合并单元格情况
            for (int i = 1; i < cellValues.length; i++) {
                for (int j = 0; j < cellValues[i].length; j++) {

                    if (cellMatrix[i][j].isColMerge() && cellMatrix[i][j].getColMergeIndex() != 0) {
                        // 如果当前单元格是跨列被合并单元格，该单元格无法自主拿主意，由合并单元格的第一列单元格决定
                        int colMergeIndex = cellMatrix[i][j].getColMergeIndex();
                        cellMatrix[i][j].setRowMerge(cellMatrix[i][colMergeIndex].isRowMerge());
                        cellMatrix[i][j].setRowMergeIndex(cellMatrix[i][colMergeIndex].getRowMergeIndex());
                        continue;
                    }

                    int preCellIndex = i - 1;
                    if (cellMatrix[preCellIndex][j].isRowMerge()) {
                        // 上行单元格已是跨行合并单元格，取合并行的第一行单元格
                        preCellIndex = i - 1 - cellMatrix[preCellIndex][j].getRowMergeIndex();
                    }
                    if (cellMatrix[preCellIndex][j].isColMerge() != cellMatrix[i][j].isColMerge()) {
                        // 上行单元格已进行的跨列合并情况与该单元格跨列合并情况不同，不支持进一步跨行合并
                        continue;
                    }
                    if (cellMatrix[preCellIndex][j].getColMergeSize() != cellMatrix[i][j].getColMergeSize()) {
                        // 上行单元格已进行的跨列合并情况与该单元格跨列合并情况不同，不支持进一步跨行合并
                        continue;
                    }
                    if (StringUtil.equalsIgcNbsX(cellValues[preCellIndex][j], cellValues[i][j])) {
                        // 更新跨行合并
                        cellMatrix[i][j].setRowMerge(true);
                        cellMatrix[i][j].setRowMergeIndex(i - preCellIndex);
                        cellMatrix[preCellIndex][j].setRowMerge(true);
                    }
                }
            }
        }
        return cellMatrix;
    }

    public void setHeadCells(String[][] heads, boolean allowColMerge, boolean allowRowMerge) {
        if (heads.length == 0) {
            throw new RuntimeException("head row size is 0");
        }
        if (heads[0].length == 0) {
            throw new RuntimeException("head column size is 0");
        }

        if (this.bodyCells != null) {
            throw new RuntimeException("head cells must set before body cells");
        }

        this.headCells = initCells(heads, allowColMerge, allowRowMerge);
        this.headRowSize = headCells.length;
        this.headColSize = headCells[0].length;
    }

    public void setBodyCells(String[][] bodys, boolean allowColMerge, boolean allowRowMerge) {
        if (bodys.length == 0) {
            this.bodyCells = new WordTableCell[0][0];
            return;
        }
        if (bodys[0].length != headColSize) {
            throw new RuntimeException("body column size not equals head column size");
        }
        bodyCells = initCells(bodys, allowColMerge, allowRowMerge);
        bodyDatas = new String[bodys.length][];
        for (int i = 0; i < bodys.length; i++) {
            bodyDatas[i] = Arrays.copyOf(bodys[i], bodys[0].length);
        }
        allowBodyColMerge = allowColMerge;
        allowBodyRowMerge = allowRowMerge;
    }

    public void addBodyCells(String... row) {
        if (row.length != headColSize) {
            throw new RuntimeException("body column size not equals head column size");
        }
        if (bodyDatas == null || bodyDatas.length == 0) {
            bodyDatas = new String[1][headColSize];
            bodyDatas[0] = Arrays.copyOf(row, headColSize);
            return;
        }
        String[][] newBodyDatas = new String[bodyDatas.length + 1][headColSize];
        for (int i = 0; i < newBodyDatas.length - 1; i++) {
            newBodyDatas[i] = bodyDatas[i];
        }
        newBodyDatas[newBodyDatas.length - 1] = Arrays.copyOf(row, headColSize);

        bodyCells = initCells(newBodyDatas, allowBodyColMerge, allowBodyRowMerge);
        bodyDatas = newBodyDatas;
    }


}
