package com.uetty.common.tool.core.word;

import com.uetty.common.tool.core.FileUtil;
import com.uetty.common.tool.core.ImageUtil;
import com.uetty.common.tool.core.string.HtmlUtil;
import com.uetty.common.tool.core.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WordUtil {

	private static final Pattern IMG_TAG_PATTERN = Pattern.compile("<img [^>]*>");

	private static final Pattern IMG_SRC_PATTERN = Pattern.compile("src[ ]*=[ ]*['\"]([^'\"]*)['\"]");

	private static final Pattern BASE64_DATA_PATTERN = Pattern.compile("data:(image/[a-zA-Z0-9]*?);base64,([\\s\\S]*)");

	/**
	 * 解析没有img标签的html，转换为非html格式的多行文本列表
	 * @param text html内容
	 * @return java.util.List<com.secfox.store.vo.word.DocXmlContent>
	 * @author Vince
	 * @date 2019/11/25 14:52
	 */
	private static List<DocXmlContent> getDocXmlContentTextLines(String text) {
		List<DocXmlContent> list = new ArrayList<>();
		boolean startsWithBreak = text.startsWith("\n");
		text = text.replaceAll("[ \n]+", " ");
		text = HtmlUtil.tagReplace(text, "div", (fullTag, headTag) -> {
			if (fullTag.length() > headTag.length() && fullTag.endsWith("</div>")) {
				return "\n" + fullTag.substring(headTag.length(), fullTag.length() - 6);
			} else {
				return "\n" + fullTag.substring(headTag.length());
			}
		});
		text = text.replace("</div>", "");
		text = text.replace("[\n]{2,}", "\n");
		text = HtmlUtil.tagReplace(text, "p", (fullTag, headTag) -> {
			if (fullTag.length() > headTag.length() && fullTag.endsWith("</p>")) {
				return "\n" + fullTag.substring(headTag.length(), fullTag.length() - 4);
			} else {
				return "\n" + fullTag.substring(headTag.length());
			}
		});
		text = text.replace("</p>", "");
		if (!startsWithBreak && text.startsWith("\n")) {
			text = text.substring(1);
		}
		text = HtmlUtil.tagReplace(text, "br", (fullTag, headTag) -> "\n");

		String[] split = text.split("\n");
		for (String str : split) {
			Document jdoc = Jsoup.parse(str);
			str = jdoc.text();
			String[] subsplit = str.split("\n");
			for (String substr : subsplit) {
				DocXmlContentText dxct = new DocXmlContentText();
				dxct.setText(substr);
				list.add(dxct);
			}
		}
		return list;
	}

	private static DocXmlContentImage parseTextToDocXmlContentImage(String text) {
		Matcher imgMathcher = IMG_SRC_PATTERN.matcher(text);
		if (imgMathcher.find()) {
			String src = imgMathcher.group(1);
			Matcher base64Matcher = BASE64_DATA_PATTERN.matcher(src);
			if (base64Matcher.matches()) {
				// base64图片
				String fileType = base64Matcher.group(1);
				String base64 = base64Matcher.group(2);

				DocXmlContentImage dxci = new DocXmlContentImage();
				dxci.setBase64(base64);
				dxci.setFileType(fileType);

				try {
					byte[] bytes = decodeLineBreakBase64(base64);

					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					int[] imageDimens = ImageUtil.getImageDimens(bais);
					dxci.setWidth((double) imageDimens[0]);
					dxci.setHeight((double) imageDimens[1]);
					bytes = null;
					return dxci;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
					return null;
				}

			} else {
				String fileType;
				try {
					// url地址
					byte[] bytes = getImageBytesFromUrl(src);
					if (bytes == null) {
						return null;
					}

					// read fileType
					byte[] magicNumberBytes = Arrays.copyOf(bytes, 8);
					String magicNumber = getFileHexMagicNumber(magicNumberBytes);
					fileType = getImageFileTypeByHexMagicNumber(magicNumber);
					if (fileType == null) {
						return null;
					}

					// read width and height
					ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
					int[] imageDimens = ImageUtil.getImageDimens(bais);

					// read base64
					String base64 = toLineBreakBase64(bytes, 76);

					DocXmlContentImage dxci = new DocXmlContentImage();
					dxci.setBase64(base64);
					dxci.setFileType(fileType);
					dxci.setWidth((double) imageDimens[0]);
					dxci.setHeight((double) imageDimens[1]);

					return dxci;
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}

	public static void processDocXmlContentImage(List<DocXmlContentImage> images, double maxWidth){
		for (int i = 0; i < images.size(); i++) {
			DocXmlContentImage dxci = images.get(i);
			// v:shape中的id使用
			dxci.setSharpId("html_image_" + (i + 1));
			int i1 = dxci.getType().indexOf("/");
			// Relationship中Target，pkg:part中contentType 使用
			dxci.setName("ext_" + (i + 1) + "." + dxci.getType().substring(i1 + 1));
			// Relationship中的Id 使用
			dxci.setRelationId("erId" + (i + 1));

			Double width = dxci.getWidth();
			if (width > maxWidth) {
				double r = dxci.getHeight() / width;
				width = maxWidth;
				double height = width * r;
				dxci.setHeight(height);
				dxci.setWidth(width);
			}
		}
	}

	private static byte[] getImageBytesFromUrl(String src) throws IOException {
		InputStream imageInputStream = ImageUtil.getInputStream(src);
		if (imageInputStream == null) {
			return null;
		}
		return FileUtil.readToByte(imageInputStream);
	}

	private static String getFileHexMagicNumber(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 4 && i < bytes.length; i++) {
			int high = (bytes[i] & 0xf0) >> 4;
			char highChar;
			if (high < 10) {
				highChar = (char) ('0' + high);
			} else {
				highChar = (char) ('A' + (high - 10));
			}
			int low = bytes[i] & 0x0f;
			char lowChar;
			if (low < 10) {
				lowChar = (char) ('0' + low);
			} else {
				lowChar = (char) ('A' + (low - 10));
			}
			sb.append(highChar).append(lowChar);
		}
		return sb.toString();
	}

	private static String getImageFileTypeByHexMagicNumber(String magicNumber) {
		if (magicNumber.startsWith("FFD8FF")) {
			return "image/jpeg";
		} else if (magicNumber.startsWith("89504E47")) {
			return "image/png";
		} else if (magicNumber.startsWith("47494638")) {
			return "image/gif";
		} else if (magicNumber.startsWith("424D")) {
			return "image/bmp";
		} else if (magicNumber.startsWith("49492A00")) {
			return "image/tiff";
		} else {
			return null;
		}
	}

	/**
	 * html代码转为word doc辅助对象（将img元素和普通文本元素分离开，并将多行普通文本拆分开为多个对象）
	 * @param text html格式的文本
	 * @param imagesIndex image索引，image类型在word模板中除展示位置外还有其他地方需要使用
	 * @return java.util.List<com.secfox.store.vo.word.DocXmlContent>
	 * @author Vince
	 * @date 2019/11/25 17:47
	 */
	public static List<DocXmlContent> parseTextToDocXmlContent(String text, List<DocXmlContentImage> imagesIndex) {
		List<DocXmlContent> list = new ArrayList<>();
		if (StringUtil.isBlank(text)) {
			return list;
		}

		Matcher matcher;
		while((matcher = IMG_TAG_PATTERN.matcher(text)).find()) {
			String group = matcher.group();
			int i = text.indexOf(group);
			if (i > 0) {
				List<DocXmlContent> textLines = getDocXmlContentTextLines(text.substring(0, i));
				list.addAll(textLines);
			}

			if (imagesIndex != null) {
				DocXmlContentImage docXmlContentImage = parseTextToDocXmlContentImage(group);
				if (docXmlContentImage != null) {
					list.add(docXmlContentImage);
					imagesIndex.add(docXmlContentImage);
				}
			}

			text = text.substring(i + group.length());
			if (text.length() == 0) {
				break;
			}
		}
		if (text.length() > 0) {
			List<DocXmlContent> textLines = getDocXmlContentTextLines(text);
			list.addAll(textLines);
		}

		return list;
	}

	@SuppressWarnings("SameParameterValue")
	private static String toLineBreakBase64(byte[] bytes, int lineLength) {
		Base64.Encoder be = Base64.getEncoder();
		String str = be.encodeToString(bytes);
		char[] chars = str.toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < chars.length; i++) {
			if (i > 0 && i % lineLength == 0) {
				sb.append("\n");
			}
			sb.append(chars[i]);
		}

		return sb.toString();
	}

	private static byte[] decodeLineBreakBase64(String base64) {
		Base64.Decoder decoder = Base64.getDecoder();
		return decoder.decode(base64.replace("\n", ""));
	}

	private static DocXmlContentImage parseInputStreamToDocXmlContent(InputStream inputStream) throws IOException {
		DocXmlContentImage dxci;
		String fileType = null;
		byte[] bytes = FileUtil.readToByte(inputStream);

		if (bytes.length == 0) {
			return null;
		}

		if (bytes.length > 8) {
			// read fileType
			byte[] magicNumberBytes = Arrays.copyOf(bytes, 8);
			String magicNumber = getFileHexMagicNumber(magicNumberBytes);
			fileType = getImageFileTypeByHexMagicNumber(magicNumber);
		}

		// read width and height
		int[] imageDimens = ImageUtil.getImageDimens(bytes);

		// read base64
		String base64 = toLineBreakBase64(bytes, 76);

		dxci = new DocXmlContentImage();
		dxci.setBase64(base64);
		dxci.setFileType(fileType);
		dxci.setWidth((double) imageDimens[0]);
		dxci.setHeight((double) imageDimens[1]);
		return dxci;
	}

	/**
	 * 本地文件转 word doc辅助对象
	 * @param filePath: 本地文件路径
	 * @return  : DocXmlContent
	 */
	public static DocXmlContentImage parseImgFileToDocXmlContent(String filePath){
		DocXmlContentImage dxci = null;

		File targetFile = new File(filePath);
		if (!targetFile.exists() || !targetFile.isFile()) {
			return null;
		}

		try (FileInputStream fis = new FileInputStream(targetFile)) {

			dxci = parseInputStreamToDocXmlContent(fis);

		} catch (Exception e) {
			log.debug(filePath);
			log.error(e.getMessage(), e);
		}
		return dxci;
	}

	/**
	 * 图片URL转 word doc辅助对象
	 * @param url: 图片url
	 * @return  : DocXmlContent
	 */
	public static DocXmlContentImage parseImgUrlToDocXmlContent(String url){
		DocXmlContentImage dxci = null;

		try (InputStream inputStream = new URL(url).openStream()) {

			dxci = parseInputStreamToDocXmlContent(inputStream);

		} catch (Exception e) {
			log.debug("invalid image url : {}", url);
			log.error(e.getMessage(), e);
		}
		return dxci;
	}

}
