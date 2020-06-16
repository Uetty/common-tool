package com.uetty.common.tool.core.excel;

import com.uetty.common.tool.core.HttpMultipartUtil;
import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * excel读取工具类
 * <p>支持范围：xls格式
 * <p>缺点：不支持xlsx格式
 * @author vince
 */
@SuppressWarnings("unused")
public class JexcelReader implements Closeable {
	
	Logger logger = Logger.getLogger(HttpMultipartUtil.class);
	
	/** workbook对象. */
    private Workbook wb;
    private InputStream is;
    private final Object closeLock = new Object();
    private volatile boolean closed = false;
	
    public JexcelReader(InputStream is) {
		try {
			this.is = is;
			wb = Workbook.getWorkbook(is);
		} catch (BiffException | IOException e) {
			throw new RuntimeException(e);
		}
	}
    
    public JexcelReader(File file) {
		try {
			wb = Workbook.getWorkbook(file);
		} catch (BiffException | IOException e) {
			throw new RuntimeException(e);
		}
	}
    
    /**
     * 获取标签页数量
     */
    public int getSheetNum() {
    	assertNoClosed();
    	return wb.getNumberOfSheets();
    }
    
    /**
     *  根据标签页序号获取标签页
     */
    public Sheet getSheetAt(int index) {
    	assertNoClosed();
    	return wb.getSheet(index);
    }
    
    /**
     * 根据标签页名称获取标签页
     */
    public Sheet getSheetByName(String name) {
    	assertNoClosed();
    	return wb.getSheet(name);
    }
    
    /**
     * 获取所有标签页名称
     */
    public String[] getSheetNames() {
    	assertNoClosed();
    	return wb.getSheetNames();
    }
    
    /**
     * 获取指定序号标签页的指定单元格
     * @param sheetNum 标签页序号
     * @param row 第几行
     * @param col 第几列
     */
    public Cell getCell(int sheetNum, int row, int col) {
    	assertNoClosed();
    	Sheet sheet = wb.getSheet(sheetNum);
    	try {
			return sheet.getCell(col, row);
    	} catch (ArrayIndexOutOfBoundsException e) {
    		return null;
    	}
    }

    /**
     * 获取指定序号标签页的指定单元格
     * @param sheetNum 标签页序号
     * @param row 第几行
     * @param col 第几列
     */
    public String getCellValue(int sheetNum, int row, int col) {
    	assertNoClosed();
    	Sheet sheet = wb.getSheet(sheetNum);
    	String value = null;
    	try {
    		Cell cell = sheet.getCell(col, row);
    		value = cell.getContents();
    	} catch(Exception ignored) {
    	}
    	return value;
    }
    
    /**
     * 将某个标签页的数据转换为二维数组
     */
    public List<List<String>> toListList(int sheetNum) {
    	assertNoClosed();
    	Sheet sheet = wb.getSheet(sheetNum);
    	List<List<String>> listList = new ArrayList<>();
    	for (int i = 0; i < sheet.getRows(); i++) {
    		Cell[] row = sheet.getRow(i);
    		List<String> list = new ArrayList<>();
			for (Cell cell : row) {
				list.add(cell.getContents());
			}
    		listList.add(list);
    	}
    	return listList;
    }
    
    /**
     * 获取某个标签页的某一行数据列表
     */
    public List<String> getRowValue(int sheetNum, int rowNum) {
    	assertNoClosed();
    	Sheet sheet = wb.getSheet(sheetNum);
    	List<String> list = new ArrayList<>();
    	Cell[] row = sheet.getRow(rowNum);
		for (Cell cell : row) {
			String contents = cell.getContents();
			list.add(contents);
		}
    	return list;
    }
    
    public CellType getCellType(int sheetNum, int row, int col) {
    	Cell cell = getCell(sheetNum, row, col);
    	if (cell == null) return null;
    	return cell.getType();
    }
    
    private void assertNoClosed() {
    	if (closed) {
    		throw new RuntimeException("cannot operate after closed");
    	}
    }
    
    @Override
    public void close() {
    	synchronized (closeLock) {
    		if (closed) return;
    		closed = true;
		}
    	if (is != null) {
    		try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	is = null;
    	if (wb != null) {
    		try {
    			wb.close();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	wb = null;
    }
    
    @Override
    protected void finalize() {
    	close();
    }
    
    /**
     * 是否已经关闭
     */
	public boolean isClosed() {
		return closed;
	}

}
