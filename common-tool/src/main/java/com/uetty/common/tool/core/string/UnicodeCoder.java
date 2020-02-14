package com.uetty.common.tool.core.string;

public class UnicodeCoder {
	/**
	 * 字符串转为unicode值，参数二是否将ASCII字符转为unicode表示
	 */
	public static String encode(String str, boolean encodeAscii) {
		StringBuilder sb = new StringBuilder();
		char[] charArray = str.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			sb.append(charToUnicodeString(c, encodeAscii));
		}
		return sb.toString();
	}

	/**
	 * unicode值转为字符串
	 */
	public static String decode(String str) throws UnsupportDecodeException {

		StringBuilder sb = new StringBuilder();
		char[] charArray = str.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (c == '\\') {
				if (i == charArray.length - 1) {
					throw new UnsupportDecodeException();
				}
				if (charArray[i + 1] == '\\') {
					sb.append("\\");
					i++;
				} else if (charArray[i + 1] == 'u') {
					if (i >= charArray.length - 5 || !isHexChar(charArray[i + 2])
							 || !isHexChar(charArray[i + 3]) || !isHexChar(charArray[i + 4])
							 || !isHexChar(charArray[i + 5])) {
						throw new UnsupportDecodeException();
					}
					String hexInt = "" + charArray[i + 2] + charArray[i + 3] + charArray[i + 4] + charArray[i + 5];
					char v = (char) Integer.valueOf(hexInt, 16).intValue();
					sb.append(v);
					i += 5;
				} else {
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	
	private static boolean isHexChar(char c) {
		if (c >= '0' && c <= '9') return true;
		if (c >= 'a' && c <= 'f') return true;
		if (c >= 'A' && c <= 'F') return true;
		return false;
	}
	
	private static String charToUnicodeString(char c, boolean encodeAscii) {
		if (c < 0x80 && !encodeAscii) {
			if (c == '\\') {
				return "\\\\";
			} else {
				return c + "";
			}
		}

		String hex = Integer.toHexString(c);
		if (c < 0x10) {
			return "\\u000" + hex;
		} else if (c < 0x100) {
			return "\\u00" + hex;
		} else if (c < 0x1000) {
			return "\\u0" + hex;
		} else {
			return "\\u" + hex;
		}
	}
	
	public static class UnsupportDecodeException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnsupportDecodeException() {
		}
	}

	public static void main(String[] args) throws UnsupportDecodeException {
		System.out.println(encode("中abcµ", true)); // \u4e2d
		System.out.println("\u4e2d"); // 不需要转换，jvm自动处理
		System.out.println(decode("\\u4e2d")); // 中
	}
}
