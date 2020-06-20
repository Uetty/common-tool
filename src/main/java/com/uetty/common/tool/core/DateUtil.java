package com.uetty.common.tool.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("unused")
public class DateUtil {

	static long oneDay = 86400000L;
	static int rawOffset = Calendar.getInstance().getTimeZone().getRawOffset(); 
	
	/**
	 * 时间格式化为字符串
	 * @param format 匹配格式
	 * @param date 时间
	 * @return 返回按匹配格式格式化后的时间字符串
	 */
	public static String format(String format, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	/**
	 * 时间格式化为字符串，默认返回空字符串
	 * @param format 匹配格式
	 * @param date 时间
	 * @return 返回按匹配格式格式化后的时间字符串
	 */
	public static String formatIgnoreNull(String format, Date date) {
		if (date == null) {
			return "";
		}
		return format(format, date);
	}
	
	/**
	 * 时间格式化为字符串，默认返回空字符串
	 * @param format 匹配格式
	 * @param timestamp 时间戳
	 * @return 返回按匹配格式格式化后的时间字符串
	 */
	public static String formatIgnoreNull(String format, Long timestamp) {
		if (timestamp == null) {
			return "";
		}
		return formatIgnoreNull(format, new Date(timestamp));
	}
	
	public static Date addDay(Date d, int day) {
		if (d == null) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_YEAR, day);
		return c.getTime();
	}
	
	/**
	 * 字符串转时间
	 * @param format 匹配格式
	 * @param date 时间
	 * @return 返回按匹配格式反序列化的日期类
	 */
	public static Date toDate(String format, String date) throws ParseException {
		if (date == null) {
			return null;
		}
		if (date.length() > format.length()) {
			date = date.substring(0, format.length());
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.parse(date);
	}

	/**
	 * 一天的起始时间戳
	 * @param date 时间
	 * @return 返回指定日期开始的第一毫秒的时间戳
	 */
	public static long timestampOfDayStart(Date date){
//		long time = date.getTime();
//		return ((time + rawOffset) / oneDay * oneDay - rawOffset);
		return dateOfDayStart(date).getTime();
	}
	
	/**
	 * 一天的起始时间
	 * @param date 时间
	 * @return 返回指定日期开始的第一毫秒的日期类
	 */
	public static Date dateOfDayStart(Date date){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}
}
