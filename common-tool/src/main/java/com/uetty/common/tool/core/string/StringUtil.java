package com.uetty.common.tool.core.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtil {

	/**
	 * 下划线命名转驼峰
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
	 * @param str
	 * @return
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

	public static boolean checkEmail(String str) {
		if(str == null || "".equals(str.trim())) {
			return false;
		}
		Pattern p = Pattern.compile("^\\w+((-\\w+)|(\\.\\w+))*\\@[A-Za-z0-9]+((\\.|-)[A-Za-z0-9]+)*\\.[A-Za-z0-9]+$");
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
		return Arrays.stream(split).filter(s -> !"".equals(str.trim())).collect(Collectors.toList());
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
}
