package com.uetty.common.tool.core.string;

/**
 * 汉字拼音缩写工具
 * 利用GB2312按拼音顺序编码的特点实现
 * @author vince
 */
public class PinYinInitialTool {
	// 简体中文的编码范围从B0A1（45217）一直到F7FE（63486）
	private static final int BEGIN = 45217;
	private static final int END = 63486;

	/**
	 * 按照声母表示，这个表是在GB2312中的出现的第一个汉字，也就是说“啊”是代表首字母a的第一个汉字。
	 * <p>i, u, v都不做声母, 自定规则跟随前面的字母</p>
 	 */
	private static final char[] INITIAL_FIRST_CHAR_ARR = { '啊', '芭', '擦', '搭', '蛾', '发', '噶', '哈', '哈', '击', '喀', '垃', '妈', '拿', '哦', '啪',
			'期', '然', '撒', '塌', '塌', '塌', '挖', '昔', '压', '匝', };

	/**
	 * 二十六个字母区间对应二十七个端点
	 * <p>GB2312码汉字区间十进制表示</p>
 	 */
	private static final int[] INITIAL_CODE_POINT_ARR = new int[27];

	/**
	 * 首字母列表
	 */
	private static final char[] INITIAL_TABLE = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'H', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', 'S', 'T', 'T', 'T', 'W', 'X', 'Y', 'Z', };

	// 初始化
	static {
		for (int i = 0; i < 26; i++) {
			// 得到GB2312码的首字母区间端点表，十进制。
			INITIAL_CODE_POINT_ARR[i] = gbValue(INITIAL_FIRST_CHAR_ARR[i]);
		}
		// 区间表结尾
		INITIAL_CODE_POINT_ARR[26] = END;
	}

	public static String cn2py(String sourceStr) {
		StringBuilder result = new StringBuilder();
		int strLength = sourceStr.length();
		int i;
		try {
			for (i = 0; i < strLength; i++) {
				result.append(char2Initial(sourceStr.charAt(i)));
			}
		} catch (Exception e) {
			result = new StringBuilder();
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 输入字符,得到他的声母,英文字母返回对应的大写字母,其他非简体汉字返回 '0' *
	 */
	public static char char2Initial(char ch) {
		// 对于数字转换
		if (ch == '0') {
			return 'L';
		}
		if (ch == '1') {
			return 'Y';
		}
		if (ch == '2') {
			return 'E';
		}
		if (ch == '3') {
			return 'S';
		}
		if (ch == '4') {
			return 'S';
		}
		if (ch == '5') {
			return 'W';
		}
		if (ch == '6') {
			return 'L';
		}
		if (ch == '7') {
			return 'Q';
		}
		if (ch == '8') {
			return 'B';
		}
		if (ch == '9') {
			return 'J';
		}

		// 对英文字母的处理：小写字母转换为大写，大写的直接返回
		if (ch >= 'a' && ch <= 'z') {
			return (char) (ch - 'a' + 'A');
		}
		if (ch >= 'A' && ch <= 'Z') {
			return ch;
		}

		// 对非英文字母的处理：转化为首字母，然后判断是否在码表范围内，
		// 若不是，则直接返回。
		// 若是，则在码表内的进行判断
		// 汉字转换首字母
		int gb = gbValue(ch);
		if ((gb < BEGIN) || (gb > END)) {
			// 在码表区间之前，直接返回
			return ch;
		}
		int i;
		for (i = 0; i < 26; i++) {
			// 判断匹配码表区间，匹配到就break,判断区间形如“[,)”
			if ((gb >= INITIAL_CODE_POINT_ARR[i]) && (gb < INITIAL_CODE_POINT_ARR[i + 1])) {
				break;
			}
		}
		if (gb == END) {
			// 补上GB2312区间最右端
			i = 25;
		}
		// 在码表区间中，返回首字母
		return INITIAL_TABLE[i];
	}

	/**
	 * 取出汉字的编码码点
	 */
	private static int gbValue(char ch) {
		String str = "";
		str += ch;
		try {
			byte[] bytes = str.getBytes("GB2312");
			if (bytes.length < 2) {
				return 0;
			}
			return (bytes[0] << 8 & 0xff00) + (bytes[1] & 0xff);
		} catch (Exception e) {
			return 0;
		}
	}

	public static void main(String[] args) {
		// System.out.println(new File("D:\\jdk1.6.0_16\\aaa").isDirectory());
		System.out.println(cn2py("赵"));
		System.out.println(PinYinInitialTool.cn2py("马超").toUpperCase());
	}
}
