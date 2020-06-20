package com.uetty.common.tool.core;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.uetty.common.tool.constant.Global;
import com.uetty.common.tool.core.pdf.ExtFontRegistry;
import freemarker.template.TemplateException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.converter.core.XWPFConverterException;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * 文档工具，根据模板生成docx、docx转pdf、docx加密等
 */
@SuppressWarnings("unused")
public class DocTool {

	private static final String tmpFileDir = Global.TMP_FILE_DIR.getValue();

	/**
	 * 生成docx文件
	 * <p>技术原理依据：docx文件本身是zip格式打包的，将zip包内部文件制作为模板，填充数据后重新打包就能生成docx文件</p>
	 * 
	 * @param docxFile docx文件（作为样式模板）
	 * @param ftlFileMap key(String): zip包内部相对根目录的路径， value(File): 制作的ftl文件
	 * @param dataMap 替换ftl模板标签的数据
	 * @return 返回创建的文档文件
	 * @throws IOException io exception
	 * @throws TemplateException template exception
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static File createDocx(File docxFile, Map<String, File> ftlFileMap, Map<String, Object> dataMap)
			throws IOException, TemplateException {

		Map<String, File> entryFileMap = new HashMap<>();
		File outFile;
		ZipFile zipFile = null;
		ZipOutputStream zipout = null;

		try {
			// freemarker处理模板文件，生成临时文件
			for (String key : ftlFileMap.keySet()) {
				File ftlFile = ftlFileMap.get(key);
				String entryFilePath = FileTool.randomFilePathByExtName(null, tmpFileDir);
				File entryFile = FreemarkerEngine.process(dataMap, ftlFile.getAbsolutePath(), entryFilePath);
				entryFileMap.put(key, entryFile);
			}

			outFile = new File(FileTool.randomFilePathByExtName("docx", tmpFileDir));

			zipFile = new ZipFile(docxFile);
			zipout = new ZipOutputStream(new FileOutputStream(outFile));

			// 将临时文件替换到docx文件下相应路径
			Enumeration<? extends ZipEntry> zipEntrys = zipFile.entries();
			while (zipEntrys.hasMoreElements()) {
				ZipEntry next = zipEntrys.nextElement();
				InputStream is = null;
				// 生成的zip包中添加文件项
				zipout.putNextEntry(new ZipEntry(next.toString()));
				// 尝试从生成的文件读取流
				File entryFile = entryFileMap.get(next.toString());
				if (entryFile != null) {
					is = new FileInputStream(entryFile);
				}
				// 从zip包中文件读取流
				if (is == null) {
					is = zipFile.getInputStream(next);
				}
				FileTool.writeFromInputStream(zipout, is);
			}
		} finally {
			for (File entryFile : entryFileMap.values()) {
				entryFile.delete();
			}
			if (zipFile != null) {
				zipFile.close();
			}
			if (zipout != null) {
				zipout.close();
			}
		}
		return outFile;
	}

	/**
	 * docx文件转pdf文件
	 * @param docxFile 输入的docx文件
	 * @param outFile 输出的pdf文件
	 * @throws XWPFConverterException xwpf convert
	 * @throws IOException io exception
	 */
	public static void docxConvertToPdf(File docxFile, File outFile) throws XWPFConverterException, IOException {
		InputStream source = new FileInputStream(docxFile);
		OutputStream target = new FileOutputStream(outFile);

		XWPFDocument doc = new XWPFDocument(source);
		// 输出设置
		PdfOptions options = PdfOptions.create();
		// 字体提供者
		ExtFontRegistry fontProvider = ExtFontRegistry.getRegistry();
		options.fontProvider(fontProvider);

		PdfConverter.getInstance().convert(doc, target, options);
	}

	/**
	 * 给docx文件添加open密码
	 * @param docxFile 输入的docx文件
	 * @param outFile 输出的docx文件
	 * @param password 要增加的密码
	 * @throws InvalidFormatException invalid format exception
	 * @throws IOException ioexception
	 * @throws GeneralSecurityException general security exception
	 */
	public static void docxAddPassword(File docxFile, File outFile, String password)
			throws InvalidFormatException, IOException, GeneralSecurityException {
		// 加密算法与加密密钥
		EncryptionInfo info = new EncryptionInfo(EncryptionMode.standard);
		Encryptor enc = info.getEncryptor();
		enc.confirmPassword(password);

		POIFSFileSystem fs = new POIFSFileSystem();
		// 获取输入到fs的输出流，并使用加密算法装饰输出流
		OutputStream os = enc.getDataStream(fs);

		// 读取docx文件，通过流输出到fs中
		try (OPCPackage opc = OPCPackage.open(docxFile, PackageAccess.READ_WRITE)) {
			opc.save(os);
		}

		// 将fs中的数据写入到输出文件
		FileOutputStream fos = new FileOutputStream(outFile);
		fs.writeFilesystem(fos);
		fos.close();
	}

	/**
	 * pdf插入水印
	 * @param inPdfPath 输入的pdf文件
	 * @param outPdfPath 输出的pdf文件
	 * @param imageLocalAddr 插入的水印图片路径
	 * @throws IOException 读写时异常
	 * @throws DocumentException 文档异常
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void insertWaterImage(String inPdfPath, String outPdfPath, String imageLocalAddr)
			throws IOException, DocumentException {
		PdfReader reader = null;
		PdfStamper stamp = null;

		try {
			reader = new PdfReader(inPdfPath);
			// 假如PDF有4页，endPDFPage值为5
			int endPdfPage = reader.getNumberOfPages() + 1;

			File outParentFile = new File(outPdfPath).getParentFile();
			if (!outParentFile.exists()) {
				outParentFile.mkdirs();
			}
			stamp = new PdfStamper(reader, new FileOutputStream(new File(outPdfPath)));
			for (int i = 1; i < endPdfPage; i++) {
				PdfContentByte under = stamp.getUnderContent(i);
				// 插入另一组水印
				Image img = Image.getInstance(imageLocalAddr);
				// 设置图片缩放比例
				img.scalePercent(78);
				// 设置图片绝对宽度
				img.scaleAbsoluteWidth(596);
				// 设置图片绝对位置
				img.setAbsolutePosition(0, 0);
				under.addImage(img);
			}
		} finally {
			if (stamp != null) {
				stamp.close();
			}
			if (reader != null) {
				reader.close();
			}
		}
	}

	/**
	 * 按指定的表头和数据，在指定的路径生成excel文件
	 * 
	 * @param path 导出路径
	 * @param sheetTitle Excel的sheet标题
	 * @param headMap 表格第一行表头,类型为LinkedHashMap
	 * @param dataList 对应的数据
	 * @return 是否导出成功
	 * @throws IllegalAccessException illegal access
	 * @throws NoSuchMethodException no such method
	 * @throws InvocationTargetException invocation target
	 */
	public static boolean exportExcel(String path, String sheetTitle, LinkedHashMap<String, String> headMap,
			List<Object> dataList) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Set<String> keySet = headMap.keySet();
		Iterator<String> iter = keySet.iterator();
		List<String> values = new ArrayList<>();
		List<String> keys = new ArrayList<>();
		while (iter.hasNext()) {
			String key = iter.next();
			values.add(headMap.get(key));
			keys.add(key);
		}
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sheetTitle);
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		// 根据表头设置列宽
		for (int i = 0; i < values.size(); i++) {
			sheet.setColumnWidth(i, (values.get(i).length() + 2) * 2 * 256);
		}
		HSSFRow row;
		row = sheet.createRow(0);
		HSSFCell cell;
		// 根据map创建表头
		for (int i = 0; i < values.size(); i++) {
			cell = row.createCell(i);
			cell.setCellValue(values.get(i));
			cell.setCellStyle(style);
		}
		// 写数据
		if (null != dataList) {
			for (int i = 0; i < dataList.size(); i++) {
				row = sheet.createRow(i + 1);
				Object gift = dataList.get(i);
				for (int j = 0; j < keys.size(); j++) {

					cell = row.createCell(j);
					String s = String.valueOf(PropertyUtils.getProperty(gift, keys.get(j)));
					if (s == null || s.equals("null")) {
						cell.setCellValue("");
					} else {
						cell.setCellValue(s);
					}
				}
			}
		}
		writeExcelFile(wb, path, sheetTitle + ".xls");
		return true;
	}

	/**
	 * 写到excel文件
	 * @param wb workbook
	 * @param path path
	 * @param fileName  file name
	 * @return is success
	 */
	@SuppressWarnings({"UnusedReturnValue", "ReturnInsideFinallyBlock"})
	public static boolean writeExcelFile(HSSFWorkbook wb, String path, String fileName) {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(path + fileName);
			wb.write(fout);
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 如果文件存在，则删除子文件，不存在则新建
	 * @param dir 删除的文件的父级目录
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void deleteExistFiles(String dir) {
		File files = new File(dir);
		// 删除之前下载过的文件，文件夹不存在时自动新建
		if (!files.exists()) {
			files.mkdirs();
		} else {
			File[] dirFile = files.listFiles();
			if (dirFile == null) {
				return;
			}
			for (File file : dirFile) {
				// 删除子文件
				file.delete();
			}
		}
	}

	/**
	 * 删除指定的文件
	 * @param file 删除的文件
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	public static void deleteExistFile(String file) {
		File files = new File(file);
		if (files.exists()) {
			files.delete();
		}

	}

	/**
	 * 写出到指定文件
	 * @param os out put stream
	 * @param file target file
	 */
	public static void outputExcel(OutputStream os, File file) {
		int fileLength = (int) file.length();
		// 如果文件长度大于0
		if (fileLength != 0) {
			// 创建输入流
			InputStream inStream;
			byte[] buf = new byte[4096];
			// 创建输出流
			try {
				inStream = new FileInputStream(file);
				int readLength;
				while (((readLength = inStream.read(buf)) != -1)) {
					os.write(buf, 0, readLength);
				}
				os.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
