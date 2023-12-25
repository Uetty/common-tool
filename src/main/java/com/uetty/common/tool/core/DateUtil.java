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

	public static Calendar toCalendar(final Date date) {
		final Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c;
	}

	public static boolean isSameDay(final Date date1, final Date date2) {
		final Calendar cal1 = toCalendar(date1);
		final Calendar cal2 = toCalendar(date2);
		return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * 计算N天之后的日期
	 */
	public static Date addDays(final Date date, final int amount) {
		final Calendar cal = toCalendar(date);
		cal.add(Calendar.DAY_OF_MONTH, amount);
		return cal.getTime();
	}

	/**
	 * 计算N月之后的日期
	 */
	public static Date addMonth(final Date date, final int amount) {
		final Calendar cal = toCalendar(date);
		cal.add(Calendar.MONTH, amount);
		return cal.getTime();
	}
	/**
	 * 计算N年之后的日期
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date addYear(final Date date, final int amount) {
		final Calendar cal = toCalendar(date);
		cal.add(Calendar.YEAR, amount);
		return cal.getTime();
	}

	public static Date addHours(Date date, int i) {
		final Calendar cal = toCalendar(date);
		cal.add(Calendar.HOUR_OF_DAY, i);
		return cal.getTime();
	}

	public static Date addMinutes(Date date, int i) {
		final Calendar cal = toCalendar(date);
		cal.add(Calendar.MINUTE, i);
		return cal.getTime();
	}

	public static Date addSeconds(Date date, int i) {
		final Calendar cal = toCalendar(date);
		cal.add(Calendar.SECOND, i);
		return cal.getTime();
	}

	public static Date addMilliSeconds(Date date, int i) {
		final Calendar cal = toCalendar(date);
		cal.add(Calendar.MILLISECOND, i);
		return cal.getTime();
	}

	/**
	 * 字符串转时间
	 * @param format 匹配格式
	 * @param date 时间
	 * @return 返回按匹配格式反序列化的日期类
	 * @throws ParseException parse exception
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

	public static long getDurationSeconds(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			return 0;
		}
		long duration = date2.getTime() - date1.getTime();
		return (duration / 1000);
	}
}
