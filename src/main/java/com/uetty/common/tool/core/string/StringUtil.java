package com.uetty.common.tool.core.string;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class StringUtil {

	static final String EMAIL_PATTERN = "^\\w+((-\\w+)|(\\.\\w+))*@[A-Za-z0-9]+(([.\\-])[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$";
	static final String BASE_URL_CATCH_PATTERN = "(?i)^(https?://[-a-zA-Z0-9]+(\\.[-a-zA-Z0-9]+)+(:\\d+)?)(/.*)?";

	static final String DOMAIN_CATCH_PATTERN = "(?i)^https?://([-a-zA-Z0-9]+(\\.[-a-zA-Z0-9]+)+)(:\\d+)?(/.*)?";

	static final String URL_PATTERN = "(?i)^https?://[-a-zA-Z0-9]+(\\.[-a-zA-Z0-9]+)+(:\\d+)?(/.*)?";
	/**
	 * 下划线命名转驼峰
	 * @param str 下划线字符串
	 * @return 驼峰字符串
	 */
	public static String underLineToCamelStyle (String str) {
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c != '_') {
				continue;
			}
			str = str.substring(0, i) + str.substring(i + 1);
			if (i < str.length()) {
				str = str.substring(0, i) + str.substring(i, i + 1).toUpperCase()
						+ str.substring(i + 1);
			}
			i--;
		}
		return str;
	}
	
	/**
	 * 驼峰命名转下划线
	 * @param str 驼峰字符串
	 * @return 下划线字符串
	 */
	public static String camelToUnderLineStyle (String str) {
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c < 'A' || c > 'Z') {
				continue;
			}
			char u = (char) (c + 32);
			str = str.substring(0, i) + '_' + u + str.substring(i + 1);
			i++;
		}
		return str;
	}

	/**
	 * 驼峰字符串改为空格分隔字符串
	 * @param str 驼峰字符串
	 * @return 空格分隔字符串
	 */
	public static String camelToBlankSeparate(String str) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c < 'A' || c > 'Z') {
				sb.append(c);
				continue;
			}
			char l = (char) (c + 32);
			if (i > 0 && i < str.length() - 1) {
				sb.append(' ');
			}
			sb.append(l);
		}
		return sb.toString();
	}

	public static <T extends CharSequence> T def(final T str, final T def) {
		return str == null ? def : str;
	}

	public static boolean checkEmail(String str) {
		if(str == null || "".equals(str.trim())) {
			return false;
		}
		Pattern p = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = p.matcher(str);
		return matcher.matches();
	}

	public static String catchBaseUrl(String str) {
		Pattern p = Pattern.compile(BASE_URL_CATCH_PATTERN);
		Matcher matcher = p.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	public static String catchDomain(String str) {
		Pattern p = Pattern.compile(DOMAIN_CATCH_PATTERN);
		Matcher matcher = p.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	public static String matchPath(String url) {
		Pattern p = Pattern.compile(URL_PATTERN);
		Matcher matcher = p.matcher(url);
		String uri = "";
		if (matcher.find()) {
			uri = matcher.group(3);
		}
		if ("".equals(uri)) {
			uri = "/";
		}
		return uri;
	}

	public static List<String> toStringList(String str, String separator) {
		if (str == null || "".equals(str.trim())) {
			return new ArrayList<>();
		}

		String[] split = str.split(separator);
		return Arrays.stream(split).filter(s -> !"".equals(s.trim())).collect(Collectors.toList());
	}

	public static List<Long> toLongList(String str, String separator) {
		if (str == null || "".equals(str.trim())) {
			return new ArrayList<>();
		}

		String[] split = str.split(separator);
		return Arrays.stream(split).map(s -> {
			Long val = null;
			try {
				val = Long.parseLong(s);
			} catch (Exception ignore) {}
			return val;
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public static List<Integer> toIntList(String str, String separator) {
		if (str == null || "".equals(str.trim())) {
			return new ArrayList<>();
		}

		String[] split = str.split(separator);
		return Arrays.stream(split).map(s -> {
			Integer val = null;
			try {
				val = Integer.parseInt(s);
			} catch (Exception ignore) {}
			return val;
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public static <T> String join(String separator, Collection<T> collection) {
		StringBuilder sb = new StringBuilder();
		for (Object o : collection) {
			if (o != null) {
				sb.append(separator).append(o);
			}
		}
		int start = 0;
		if (sb.length() > 0) {
			start = separator.length();
		}
		return sb.substring(start);
	}

	public static <T> String join(String separator, T[] array) {
		StringBuilder sb = new StringBuilder();
		for (T t : array) {
			if (t != null) {
				sb.append(separator).append(t);
			}
		}
		int start = 0;
		if (sb.length() > 0) {
			start = separator.length();
		}
		return sb.substring(start);
	}

	public static String join(String separator, String... array) {
		StringBuilder sb = new StringBuilder();
		for (String str : array) {
			if (str != null) {
				sb.append(separator).append(str);
			}
		}
		int start = 0;
		if (sb.length() > 0) {
			start = separator.length();
		}
		return sb.substring(start);
	}

	public static String join(String separator, int... array) {
		StringBuilder sb = new StringBuilder();
		for (int n : array) {
			sb.append(separator).append(n);
		}
		int start = 0;
		if (sb.length() > 0) {
			start = separator.length();
		}
		return sb.substring(start);
	}

	public static String join(String separator, long... array) {
		StringBuilder sb = new StringBuilder();
		for (long n : array) {
			sb.append(separator).append(n);
		}
		int start = 0;
		if (sb.length() > 0) {
			start = separator.length();
		}
		return sb.substring(start);
	}

	private static boolean isNumBelow256(String str) {
		return str.matches("(1[0-9]{2})|(2[0-4][0-9])|(25[0-5])|([1-9]?[0-9])");
	}

	public static boolean isInternetAddress(String address) {
		if (address == null) {
			return false;
		}

		String[] split = address.split("\\.");
		if (split.length == 4) {
			boolean match = true;
			for (int i = split.length - 1; i >= 0; i--) {
				if (!isNumBelow256(split[i])) {
					match = false;
					break;
				}
			}
			if (match) {
				return true;
			}
		}
		if (split.length <= 1) {
			return false;
		}
		if (!split[split.length - 1].matches("(?i)[a-z]+")) {
			return false;
		}
		for (int i = 0; i < split.length - 1; i++) {
			if (!split[i].matches("(?i)[-a-z0-9]+")) {
				return false;
			}
		}
		return true;
	}

	public static boolean startsWithIgnoreCase(String str, String prefix) {
		return (str != null && prefix != null && str.length() >= prefix.length() &&
				str.regionMatches(true, 0, prefix, 0, prefix.length()));
	}

	public static boolean startsWith(String str, String prefix) {
		return (str != null && prefix != null && str.length() >= prefix.length() &&
				str.regionMatches(false, 0, prefix, 0, prefix.length()));
	}

	public static boolean isBlank(String str) {
		return str == null || "".equals(str.trim());
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}

	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static boolean equals(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		}
		if (str1 == null || str2 == null) {
			return false;
		}
		if (str1.length() != str2.length()) {
			return false;
		}
		return str1.equals(str2);
	}

	public static boolean equalsIgc(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		}
		if (str1 == null || str2 == null) {
			return false;
		}
		if (str1.length() != str2.length()) {
			return false;
		}
		return str1.equalsIgnoreCase(str2);
	}

	/**
	 * 空值安全的equals比较（去除前后空白字符后比较）
	 */
	public static boolean equalsNbs(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		}
		if (str1 == null || str2 == null) {
			return false;
		}
		str1 = str1.trim();
		str2 = str2.trim();
		if (str1.length() != str2.length()) {
			return false;
		}
		return str1.equals(str2);
	}

	public static String trim(String str) {
		return str == null ? null : str.trim();
	}

	/**
	 * 增强的trim，也去除掉前后的不间断空格与中文全角空格
	 */
	public static String trimX(String str) {
		if (str == null) {
			return str;
		}
		int len = str.length();
		int st = 0;
		char[] val = str.toCharArray();

		// 不间断空格
		char noBreakSpace = '\u00A0';
		// 全角空格
		char fullAngleSpace = '\u3000';
		while ((st < len) && (val[st] <= ' ' || val[st] == fullAngleSpace || val[st] == noBreakSpace)) {
			st++;
		}
		while ((st < len) && (val[len - 1] <= ' ' || val[len - 1] == fullAngleSpace || val[len - 1] == noBreakSpace)) {
			len--;
		}
		return ((st > 0) || (len < str.length())) ? str.substring(st, len) : str;
	}

	/**
	 * 空值安全的equals比较（忽略大小写，去除前后空白字符）
	 */
	public static boolean equalsIgcNbs(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		}
		if (str1 == null || str2 == null) {
			return false;
		}
		str1 = str1.trim();
		str2 = str2.trim();
		if (str1.length() != str2.length()) {
			return false;
		}
		return str1.equalsIgnoreCase(str2);
	}


	/**
	 * 空值安全的equals比较（忽略大小写，去除前后空白字符）
	 */
	public static boolean equalsIgcNbsX(String str1, String str2) {
		if (str1 == null && str2 == null) {
			return true;
		}
		if (str1 == null || str2 == null) {
			return false;
		}
		str1 = trimX(str1);
		str2 = trimX(str2);
		if (str1.length() != str2.length()) {
			return false;
		}
		return str1.equalsIgnoreCase(str2);
	}

	/**
	 * 参数化字符串计算（允许嵌套参数，如：a=c,b=d,cd=11时，“${${a}${b}}-${b}”解析后为11-d）
	 * @param templateStr 模版字符串
	 * @param params 参数
	 * @return 计算结果字符串
	 */
	public static String parametricCalc(String templateStr, Map<String, String> params) {
		if (templateStr == null) {
			return templateStr;
		}

		char[] chars = templateStr.toCharArray();
		char escape = '\\';
		char open1 = '$';
		char open2 = '{';
		char close = '}';

		// 参数栈深度记录
		int stackSize = 0;

		StringBuilder sb = new StringBuilder();

		// 已处理源字符数量
		int solved = 0;

		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c == escape) {
				// 当前是转义字符，同时处理两个字符，后一个字符为转义后的字符
				if (stackSize == 0) {
					// 没有待处理栈，表明是正常字符串，直接存入后一个字符
					if (i + 1 < chars.length) {
						sb.append(chars[i + 1]);
					}
					// 标记处理了2个字符
					solved += 2;
				}
				i++;
			} else if (c == open1) {
				// 当前字符是 $
				if (i + 1 < chars.length && chars[i + 1] == open2) {
					// 当前前两个字符是${，标记栈深度+1，并跳过后一个字符
					stackSize++;
					i++;
				} else if (stackSize == 0) {
					// 当前没有待处理栈
					sb.append(c);
					solved += 1;
				}
				// 有待处理栈时，则不标记为已处理，等待栈关闭标签匹配完再处理
			} else if (c == close) {
				if (stackSize > 1) {
					// 不是最外层栈，栈深度减1
					stackSize--;
				} else if (stackSize == 1) {
					// 已经是最外层栈，使用递归处理参数值
					String subStr = new String(chars, solved + 2, i - solved - 2);
					String paramKey = parametricCalc(subStr, params);
					String value = params.get(paramKey);
					if (value != null) {
						// 获取到参数值，显示转化后的值
						sb.append(value);
					} else {
						// 无该参数，显示原参数字符串
						sb.append(open1);
						sb.append(open2);
						sb.append(paramKey);
						sb.append(close);
					}
					solved = i + 1;
					stackSize = 0;
				} else {
					sb.append(c);
					solved += 1;
				}
			} else {
				if (stackSize == 0) {
					sb.append(c);
					solved += 1;
				}
			}
		}

		if (solved < chars.length) {
			// "${" 比 "}" 的数量更多，导致不能正常结束，消化掉第一个字符后再次尝试
			sb.append(chars[solved]);
			sb.append(parametricCalc(new String(chars, solved + 1, chars.length - solved - 1), params));
		}

		return sb.toString();
	}
}
