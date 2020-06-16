package com.uetty.common.tool.core;

import org.slf4j.Logger;

@SuppressWarnings("unused")
public class LogTool {

	static String CONTAIN_STR = "uetty";
	
	
	/**
	 * 打印完整类名包含字符串"uetty"的日志
	 */
	public static void printStackTraceOnlyUetty(Logger logger, Throwable e) {
		printStackTraceOnlyClassNameContain(logger, e, CONTAIN_STR, null);
	}
	
	/**
	 * 打印完整类名包含特定字符串的日志
	 */
	public static void printStackTraceOnlyClassNameContain(Logger logger, Throwable e, String containStr) {
		printStackTraceOnlyClassNameContain(logger, e, containStr, null);
	}
	
	/**
	 * 仅打印完整类名包含指定字符串的日志
	 */
	private static void printStackTraceOnlyClassNameContain(Logger logger, Throwable e, String containStr,  StringBuffer sb) {
		if (e == null) return;
		if (sb == null) {
			sb = new StringBuffer();
		}
		sb.append("Exception : ").append(e.getMessage()).append("\n");
		StackTraceElement[] stackTraces = e.getStackTrace();
		for (StackTraceElement trace : stackTraces) {
			if (trace.getClassName().contains("starnet")) {
				sb.append("\tat ").append(trace.toString()).append("\n");
			}
		}
		Throwable[] suppressed = e.getSuppressed();
		for (Throwable throwable : suppressed) {
			printStackTraceOnlyClassNameContain(logger, throwable, containStr, sb);
		}
		Throwable cause = e.getCause();
		printStackTraceOnlyClassNameContain(logger, cause, containStr, sb);
		if (suppressed.length == 0 && cause == null) {
			logger.error(sb.toString());
		}
	}
}
