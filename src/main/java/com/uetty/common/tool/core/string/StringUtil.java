package com.uetty.common.tool.core.string;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class StringUtil {

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

	public static boolean checkEmail(String str) {
		if(str == null || "".equals(str.trim())) {
			return false;
		}
		Pattern p = Pattern.compile("^\\w+((-\\w+)|(\\.\\w+))*@[A-Za-z0-9]+(([.\\-])[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$");
		Matcher matcher = p.matcher(str);
		return matcher.matches();
	}

	public static String matchSiteAddress(String str) {
		Pattern p = Pattern.compile("(?i)^(https?://[-a-zA-Z0-9]+(\\.[-a-zA-Z0-9]+)+(:\\d+)?)(/.*)?");
		Matcher matcher = p.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	public static String matchAddress(String str) {
		Pattern p = Pattern.compile("(?i)^https?://([-a-zA-Z0-9]+(\\.[-a-zA-Z0-9]+)+)(:\\d+)?(/.*)?");
		Matcher matcher = p.matcher(str);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	public static String matchPath(String url) {
		Pattern p = Pattern.compile("(?i)^https?://[-a-zA-Z0-9]+(\\.[-a-zA-Z0-9]+)+(:\\d+)?(/.*)?");
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
		if (str == null || "".equals(str.trim())) return new ArrayList<>();

		String[] split = str.split(separator);
		return Arrays.stream(split).filter(s -> !"".equals(s.trim())).collect(Collectors.toList());
	}

	public static List<Long> toLongList(String str, String separator) {
		if (str == null || "".equals(str.trim())) return new ArrayList<>();

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
		if (str == null || "".equals(str.trim())) return new ArrayList<>();

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
}
