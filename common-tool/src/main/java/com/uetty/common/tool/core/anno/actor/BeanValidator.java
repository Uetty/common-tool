package com.uetty.common.tool.core.anno.actor;

import com.uetty.common.tool.core.DateUtil;
import com.uetty.common.tool.core.anno.valid.*;
import com.uetty.common.tool.core.string.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;


/**
 * 对注解验参的支持
 * @author vince
 * <p>为了方便阅读，这里的中文暂时没有用unicode编码，正式使用的时候再修改掉
 */
public class BeanValidator {

	/**
	 * 根据注解自动验参
	 */
	public static void validate(Object obj) {
		if (obj == null) {
			return;
		}
		Class<? extends Object> clz = obj.getClass();
		Method[] methods = clz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (!isGetter(method)) {
				continue;
			}
			String name = method.getName();
			String fieldName = name.substring(3, 4).toLowerCase() + name.substring(4);
			Object value = null;
			try {
				value = method.invoke(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Map<String, Annotation> annoMap = getAnnotations(method, clz);
			if(annoMap.size() <= 0) {
				continue;
			}
			// @Digit注解处理 （数字范围）
			checkDigit(value, fieldName, annoMap);
			// @Email注解处理 （邮件地址）
			checkEmail(value, fieldName, annoMap);
			// @Future注解处理 （在某时间之后）
			// 支持附加@Timestring注解用以解释时间字符串格式化规则，无@Timesting注解，将尝试默认格式化规则
			checkFuture(value, fieldName, annoMap);
			// @Length注解处理 （字符串长度范围）
			checkLength(value, fieldName, annoMap);
			// @Size注解处理 （list长度范围）
			checkSize(value, fieldName, annoMap);
			// @LimitEnum注解处理 （字符串取值限制为给定enum的name()值集合）
			checkLimitEnum(value, fieldName, annoMap);
			// @Limit注解处理 （字符串取值限制为给定字符串数组里的值）
			checkLimit(value, fieldName, annoMap);
			// @Max注解处理 （数字最大值）
			checkMax(value, fieldName, annoMap);
			// @Min注解处理 （数字最小值）
			checkMin(value, fieldName, annoMap);
			// @NotBlank注解处理 （字符串不能为空白）
			checkNotBlank(value, fieldName, annoMap);
			// @NotEmpty注解处理 （字符串不能为空）
			checkNotEmpty(value, fieldName, annoMap);
			// @NotNull注解处理 （不能为null）
			checkNotNull(value, fieldName, annoMap);
			// @Past注解处理 （在某时间之前）
			// 支持附加@Timestring注解用以解释时间字符串格式化规则，无@Timesting注解，将尝试默认格式化规则
			checkPast(value, fieldName, annoMap);
			// @Pattern注解处理 （字符串满足给定正则规则）
			checkPattern(value, fieldName, annoMap);
			// @Timestring注解处理 （时间字符串满足给定格式）
			checkTimestring(value, fieldName, annoMap);
			// @IsId注解处理 （数字大于0）
			checkIsId(value, fieldName, annoMap);
			if (isABean(annoMap)) {
				validate(value);
			}
		}
	}

	/**
	 * 如果有特殊需求，需对消息进行国际化等情况，修改这里的代码
	 */
	private static String getMsg(String msg) {
		return msg;
	}
	
	/**
	 * 判断是否getter方法
	 */
	private static boolean isGetter(Method method) {
		boolean isGetterName = method.getName().startsWith("get") && method.getName().length() > 3;
		if (!isGetterName) {
			return false;
		}
		if("getClass".equals(method.getName())) {
			return false;
		}
		return true;
	}
	
	/**
	 * 获取所有注解
	 */
	private static Map<String, Annotation> getAnnotations(Method method, Class<? extends Object> clz) {
		Map<String, Annotation> map = new HashMap<String, Annotation>();
		String name = method.getName();
		String fieldName = name.substring(3, 4).toLowerCase() + name.substring(4);
		Annotation[] annotations = method.getAnnotations();
		for (int i = 0; i < annotations.length; i++) {
			Annotation annotation = annotations[i];
			Class<? extends Annotation> annotationType = annotation.annotationType();
			map.put(annotationType.getName(), annotation);
		}
		try {
			Field declaredField = clz.getDeclaredField(fieldName);
			if (declaredField != null) {
				Annotation[] fieldAnnotations = declaredField.getAnnotations();
				for (int i = 0; i < fieldAnnotations.length; i++) {
					Annotation annotation = fieldAnnotations[i];
					Class<? extends Annotation> annotationType = annotation.annotationType();
					map.put(annotationType.getName(), annotation);
				}
			} else {
				Field field = clz.getField(fieldName);
				if(field != null) {
					Annotation[] fieldAnnotations = field.getDeclaredAnnotations();
					for (int i = 0; i < fieldAnnotations.length; i++) {
						Annotation annotation = fieldAnnotations[i];
						Class<? extends Annotation> annotationType = annotation.annotationType();
						map.put(annotationType.getName(), annotation);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * 判断是否扫描内部变量
	 */
	private static boolean isABean(Map<String, Annotation> annoMap) {
		Annotation annotation = annoMap.get(Valid.class.getName());
		return annotation != null;
	}
	
	/**
	 * 是否数值
	 */
	private static boolean isNumber(Object obj) {
		if(obj == null) {
			return false;
		}
		return obj instanceof Number;
	}
	
	private static void checkDigit(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Digit anno = (Digit) annoMap.get(Digit.class.getName());
		if (anno != null) {
			String msg = anno.msg();
			if(StringUtils.isBlank(msg)) {
				msg = anno.msg() + "[当前值为" + value + "]";
			}
			if(value == null || !isNumber(value)) {
				throw new ParamInvalidException(msg);
			}
			Number num = (Number) value;
			if (num.doubleValue() > anno.max() || num.doubleValue() < anno.min()) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkEmail(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Email anno = (Email) annoMap.get(Email.class.getName());
		if(anno != null) {
			String val = value + "";
			if(!StringUtil.checkEmail(val)) {
				String msg = getMsg(anno.msg());
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkFuture(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Future anno = (Future) annoMap.get(Future.class.getName());
		if(anno != null) {
			String msg = getMsg(anno.msg());
			long time = anno.time();
			Date date = null;
			if(isNumber(value)) {
				Number num = (Number) value;
				date = new Date(num.longValue());
			} else {
				@SuppressWarnings("unlikely-arg-type")
				Timestring timeAnno = (Timestring) annoMap.get(Timestring.class);
				if (timeAnno != null) {
					date = checkTimestring(value, fieldName, annoMap);
				} else {
					String format = null;
					String val = ("" + value);
					if(val.contains("-")) {
						if (val.length() == 10) {
							format = "yyyy-MM-dd";
						} else if(val.length() >= 19) {
							val = val.substring(0, 19);
							format = "yyyy-MM-dd HH:mm:ss";
						} else if(val.length() == 16) {
							format = "yyyy-MM-dd HH:mm";
						} else if(val.length() == 7) {
							format = "yyyy-MM";
						}
					}
					if(val.contains("/")) {
						if (val.length() == 10) {
							format = "yyyy/MM/dd";
						} else if(val.length() >= 19) {
							val = val.substring(0, 19);
							format = "yyyy/MM/dd HH:mm:ss";
						} else if(val.length() == 16) {
							format = "yyyy/MM/dd HH:mm";
						} else if(val.length() == 7) {
							format = "yyyy/MM";
						}
					}
					if(val.length() == 8) {
						format = "HH:mm:ss";
					}
					if(format == null) {
						throw new ParamInvalidException(msg);
					}
					try {
						date = DateUtil.toDate(format, val);
					} catch (ParseException e) {
						throw new ParamInvalidException(msg);
					}
				}
			}
			if(date == null || date.getTime() < time) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkPast(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Past anno = (Past) annoMap.get(Past.class.getName());
		if(anno != null) {
			String msg = getMsg(anno.msg());
			long time = anno.time();
			Date date = null;
			if(isNumber(value)) {
				Number num = (Number) value;
				date = new Date(num.longValue());
			} else {
				@SuppressWarnings("unlikely-arg-type")
				Timestring timeAnno = (Timestring) annoMap.get(Timestring.class);
				if (timeAnno != null) {
					date = checkTimestring(value, fieldName, annoMap);
				} else {
					String format = null;
					String val = ("" + value);
					if(val.contains("-")) {
						if (val.length() == 10) {
							format = "yyyy-MM-dd";
						} else if(val.length() >= 19) {
							val = val.substring(0, 19);
							format = "yyyy-MM-dd HH:mm:ss";
						} else if(val.length() == 16) {
							format = "yyyy-MM-dd HH:mm";
						} else if(val.length() == 7) {
							format = "yyyy-MM";
						}
					}
					if(val.contains("/")) {
						if (val.length() == 10) {
							format = "yyyy/MM/dd";
						} else if(val.length() >= 19) {
							val = val.substring(0, 19);
							format = "yyyy/MM/dd HH:mm:ss";
						} else if(val.length() == 16) {
							format = "yyyy/MM/dd HH:mm";
						} else if(val.length() == 7) {
							format = "yyyy/MM";
						}
					}
					if(val.length() == 8) {
							format = "HH:mm:ss";
					}
					if(format == null) {
						throw new ParamInvalidException(msg);
					}
					try {
						date = DateUtil.toDate(format, val);
					} catch (ParseException e) {
						throw new ParamInvalidException(msg);
					}
				}
			}
			if(date == null || date.getTime() > time) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkLength(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Length anno = (Length) annoMap.get(Length.class.getName());
		if(anno != null) {
			String msg = getMsg(anno.msg());
			if(value == null || ("" + value).length() > anno.max() || ("" + value).length() < anno.min()) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void checkSize(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Size anno = (Size) annoMap.get(Size.class.getName());
		if(anno != null && value instanceof List) {
			String msg = getMsg(anno.msg());
			List<Object> list = (List<Object>) value;
			if(list.size() > anno.max() || list.size() < anno.min()) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkLimitEnum(Object value, String fieldName, Map<String, Annotation> annoMap) {
		LimitEnum anno = (LimitEnum) annoMap.get(LimitEnum.class.getName());
		if(anno != null) {
			String msg = getMsg(anno.msg());
			Class<? extends Enum<?>> enums = anno.enums();
			if(enums.isEnum()) {
				Object[] enumConstants = enums.getEnumConstants();
				boolean contain = false;
				for (int i = 0; i < enumConstants.length; i++) {
					Object object = enumConstants[i];
					if(object.toString().equals(value + "")) {
						contain = true;
						break;
					}
				}
				if(!contain) {
					throw new ParamInvalidException(msg);
				}
			}
		}
	}
	
	private static void checkLimit(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Limit anno = (Limit) annoMap.get(Limit.class.getName());
		if(anno != null) {
			String msg = getMsg(anno.msg());
			String[] values = anno.values();
			boolean contain = false;
			for (int i = 0; i < values.length; i++) {
				String val = values[i];
				if (val != null) {
					if(val.equals(value + "")) {
						contain = true;
						break;
					}
				} else {
					if(value == null) {
						contain = true;
						break;
					}
				}
			}
			if(!contain) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkMax(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Max anno = (Max) annoMap.get(Max.class.getName());
		if (anno != null) {
			String msg = getMsg(anno.msg());
			if(value == null || !isNumber(value)) {
				throw new ParamInvalidException(msg);
			}
			Number num = (Number) value;
			if (num.doubleValue() > anno.value()) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkMin(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Min anno = (Min) annoMap.get(Min.class.getName());
		if (anno != null) {
			String msg = getMsg(anno.msg());
			if(value == null || !isNumber(value)) {
				throw new ParamInvalidException(msg);
			}
			Number num = (Number) value;
			if (num.doubleValue() < anno.value()) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkNotBlank(Object value, String fieldName, Map<String, Annotation> annoMap) {
		NotBlank anno = (NotBlank) annoMap.get(NotBlank.class.getName());
		if (anno != null) {
			String msg = getMsg(anno.msg());
			if(!(value instanceof String)) {
				throw new ParamInvalidException(msg);
			}
			String val = (String) value;
			if(StringUtils.isBlank(val)) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkNotEmpty(Object value, String fieldName, Map<String, Annotation> annoMap) {
		NotEmpty anno = (NotEmpty) annoMap.get(NotEmpty.class.getName());
		if (anno != null) {
			String msg = getMsg(anno.msg());
			if(!(value instanceof String)) {
				throw new ParamInvalidException(msg);
			}
			String val = (String) value;
			if(StringUtils.isEmpty(val)) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkNotNull(Object value, String fieldName, Map<String, Annotation> annoMap) {
		NotNull anno = (NotNull) annoMap.get(NotNull.class.getName());
		if (anno != null) {
			String msg = getMsg(anno.msg());
			if(value == null) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static void checkPattern(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Pattern anno = (Pattern) annoMap.get(Pattern.class.getName());
		if (anno != null) {
			String msg = getMsg(anno.msg());
			try {
				String val = value + "";
				java.util.regex.Pattern p = java.util.regex.Pattern.compile(anno.regexp());
				Matcher matcher = p.matcher(val);
				boolean matches = matcher.matches();
				if(!matches) {
					throw new ParamInvalidException(msg);
				}
			} catch(Exception e) {
				throw new ParamInvalidException(msg);
			}
		}
	}
	
	private static Date checkTimestring(Object value, String fieldName, Map<String, Annotation> annoMap) {
		Timestring anno = (Timestring) annoMap.get(Timestring.class.getName());
		if (anno != null) {
			String msg = getMsg(anno.msg());
			String format = anno.format();
			String val = ("" + value);
			try {
				return DateUtil.toDate(format, val);
			} catch (ParseException e) {
				throw new ParamInvalidException(msg);
			}
		}
		return null;
	}
	
	private static void checkIsId(Object value, String fieldName, Map<String, Annotation> annoMap) {
		IsId anno = (IsId) annoMap.get(IsId.class.getName());
		if (anno != null) {
			String msg = getMsg(anno.msg());
			if(!isNumber(value)) {
				throw new ParamInvalidException(msg);
			}
			Number num = (Number) value;
			if(num.longValue() <= 0) {
				throw new ParamInvalidException(msg);
			}
		}
	}
}


