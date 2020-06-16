package com.uetty.common.tool.core.excel;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author caihua
 */
@SuppressWarnings("unused")
public class XlsReader {
	
	Logger logger = LoggerFactory.getLogger(XlsReader.class);
	
	private HSSFWorkbook wb = null;// book [includes sheet]

	private HSSFSheet sheet = null;

	private HSSFRow row = null;

	private int sheetNum = 0; // 第sheetnum个工作表

	private int rowNum = 0;

	private File file = null;

	public XlsReader() {
	}

	public XlsReader(File file) {
		this.file = file;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

	public void setSheetNum(int sheetNum) {
		this.sheetNum = sheetNum;
	}

	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * 读取excel文件获得HSSFWorkbook对象
	 */
	public void open() throws IOException {
		FileInputStream fis = new FileInputStream(file);
		wb = new HSSFWorkbook(new POIFSFileSystem(fis));
		fis.close();
	}

	/**
	 * 返回sheet表数目
	 * 
	 * @return int
	 */
	public int getSheetCount() {
		int sheetCount;
		sheetCount = wb.getNumberOfSheets();
		return sheetCount;
	}

	/**
	 * sheetNum下的记录行数
	 * 
	 * @return int
	 */
	public int getRowCount() {
		if (wb == null)
			logger.error("workbook is null");
		HSSFSheet sheet = wb.getSheetAt(this.sheetNum);
		int rowCount;
		rowCount = sheet.getLastRowNum();
		return rowCount;
	}

	/**
	 * 读取指定sheetNum的rowCount
	 */
	public int getRowCount(int sheetNum) {
		HSSFSheet sheet = wb.getSheetAt(sheetNum);
		int rowCount;
		rowCount = sheet.getLastRowNum();
		return rowCount;
	}

	/**
	 * 得到指定行的内容
	 */
	public String[] readExcelLine(int lineNum) {
		return readExcelLine(this.sheetNum, lineNum);
	}

	/**
	 * 指定工作表和行数的内容
	 */
	public String[] readExcelLine(int sheetNum, int lineNum) {
		if (sheetNum < 0 || lineNum < 0)
			return null;
		String[] strExcelLine = null;
		try {
			sheet = wb.getSheetAt(sheetNum);
			row = sheet.getRow(lineNum);

			int cellCount = row.getLastCellNum();
			strExcelLine = new String[cellCount];
			for (int i = 0; i <cellCount; i++) {
				strExcelLine[i] = readStringExcelCell(lineNum, i);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return strExcelLine;
	}

	/**
	 * 读取指定列的内容
	 */
	public String readStringExcelCell(int cellNum) {
		return readStringExcelCell(this.rowNum, cellNum);
	}

	/**
	 * 指定行和列编号的内容
	 */
	public String readStringExcelCell(int rowNum, int cellNum) {
		return readStringExcelCell(this.sheetNum, rowNum, cellNum);
	}

	/**
	 * 指定工作表、行、列下的内容
	 */
	public String readStringExcelCell(int sheetNum, int rowNum, int cellNum) {
		if (sheetNum < 0 || rowNum < 0)
			return "";
		String strExcelCell = "";
		try {
			sheet = wb.getSheetAt(sheetNum);
			row = sheet.getRow(rowNum);
			if (row.getCell(cellNum) != null) { // add this condition
				// judge
				switch (row.getCell(cellNum).getCellType()) {
				case HSSFCell.CELL_TYPE_FORMULA:
					strExcelCell = "FORMULA ";
					break;
				case HSSFCell.CELL_TYPE_NUMERIC: {
					double d= row.getCell(cellNum).getNumericCellValue();
					BigDecimal db = new BigDecimal(d);
					strExcelCell =  db.toPlainString();
				}
					break;
				case HSSFCell.CELL_TYPE_STRING:
					strExcelCell = row.getCell(cellNum).getStringCellValue();
					break;
				case HSSFCell.CELL_TYPE_BLANK:
					strExcelCell = "";
					break;
				default:
					strExcelCell = "";
					break;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return strExcelCell;
	}
	
	public static List<Object[]> getList(File file,int startRow,int readClm) throws IOException{
		List<Object[]> list= new ArrayList<>();
		XlsReader readExcel = new XlsReader(file);
		readExcel.open();
		readExcel.setSheetNum(0); // 设置读取索引为0的工作表
		// 总行数
		int count = readExcel.getRowCount();
		for (int i = startRow; i <= count; i++) {
			String[] rows = readExcel.readExcelLine(i);
			Object[] obj=new Object[readClm];
			int flagLength= Math.min(rows.length, readClm);
			if (flagLength >= 0) System.arraycopy(rows, 0, obj, 0, flagLength);
			for(int j = flagLength; j< readClm; j++){
				obj[j]="";
			}
			list.add(obj);
		}
		return list;
	}

	public static void main(String[] args) {
		File file = new File("D:\\template.xls");
		XlsReader readExcel = new XlsReader(file);
		try {
			readExcel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		readExcel.setSheetNum(0); // 设置读取索引为0的工作表
		// 总行数
		int count = readExcel.getRowCount();
		for (int i = 1; i <= count; i++) {
			String[] rows = readExcel.readExcelLine(i);
			for (String s : rows) {
				System.out.print(s + " ");
			}
			System.out.print("\n");
		}
	}
}