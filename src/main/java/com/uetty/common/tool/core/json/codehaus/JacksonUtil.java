package com.uetty.common.tool.core.json.codehaus;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import java.io.IOException;

/** 
 * codehaus版本jackson工具类（旧版jackson，新版是fasterxml）
 * @author vince
 */
@SuppressWarnings("unused")
public class JacksonUtil {

	private final ObjectMapper mapper = new ObjectMapper();
	
    /**
     * 反序列化时，float转为成BigDecimal
     * @return 链式返回
     */
    public JacksonUtil withBigDecimalForFloats() {
        this.mapper.configure(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        return this;
    }

    /**
     * 反序列化时，空字符串转为null
     * <p>这里容易理解有歧义，java类属性不是String的时候会出现null，否则还是空字符串
     * @return 链式返回
     */
    public JacksonUtil withEmptyStringAsNull() {
    	this.mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return this;
    }
    
    /**
     * 序列化时，忽略Map中的空值条目
     * @return 链式返回
     */
    public JacksonUtil withDisWriteNullMapValue() {
    	this.mapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
    	return this;
    }

    /**
     * 反序列化时，java基本类型不接受空值
     * @return 链式返回
     */
    public JacksonUtil withDisAcceptNullPrimitive() {
    	this.mapper.configure(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
    	return this;
    }

    /**
     * 反序列化时，非数组值能够被强转为数组
     * @return 链式返回
     */
    public JacksonUtil withSingleValueAsArray() {
        this.mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return this;
    }
    

    /**
     * 反序列化时，忽略未知属性
     * @return 链式返回
     */
    public JacksonUtil withDisFailUnknow() {
    	this.mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return this;
    }


    /**
     * 序列化时，缩进输出字符串
     * <p>缩进输出，即格式化后输出，更利于查看
     * @return 链式返回
     */
    public JacksonUtil withIndentOutput() {
    	this.mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        return this;
    }

    /**
     * 序列化时，时间不转为时间戳
     * @return 链式返回
     */
    public JacksonUtil withDisDateToTimestamp() {
    	this.mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        return this;
    }

    /**
     * 序列化时，属性输出按字母顺序排列
     * @return 链式返回
     */
    public JacksonUtil withSortFieldNames() {
    	this.mapper.configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true);
        return this;
    }

    /**
     *  序列化时，不能处理的类型，赋值null
     * @return 链式返回
     */
    public JacksonUtil withIgnoreEmptyBeans() {
    	this.mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        return this;
    }

    /**
     * 序列化/反序列化时，不重新包装序列化时捕获到的异常，而直接抛出
     * @return 链式返回
     * @since 1.7
     */
    public JacksonUtil withDisWrapExceptions() {
    	this.mapper.configure(SerializationConfig.Feature.WRAP_EXCEPTIONS, false);
    	this.mapper.configure(DeserializationConfig.Feature.WRAP_EXCEPTIONS, false);
        return this;
    }

    /**
     * 序列化/反序列化时，使用toString()方法序列化/反序列化枚举类型（未设置时，默认使用name()方法）
     * @return 链式返回
     */
    public JacksonUtil withEnumToString() {
    	this.mapper.configure(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, true);
    	this.mapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);
        return this;
    }

    /**
     * 自定义类型转换的支持
     * @param module 自定义module
     * @return 链式返回
     */
    public JacksonUtil registerModule(Module module) {
    	this.mapper.registerModule(module);
    	return this;
    }
    
    /**
     * 将Object对象转化成json字符串
     * @param obj 对象实例
     * @return 序列化输出
     * @throws IOException io exception
     */
    public String obj2Json(Object obj) throws IOException {
        return this.mapper.writeValueAsString(obj);
    }

    /**
     * 将Object对象转化成byte数组
     * @param obj 对象实例
     * @return 序列化输出
     * @throws IOException io exception
     */
    public byte[] obj2Byte(Object obj) throws IOException {
        return this.mapper.writeValueAsBytes(obj);
    }

    /**
     * 将json字符串转化成bean对象
     * @param <T> 反序列化后的类型
     * @param json 字符串
     * @param t 解析目标类
     * @return 反序列化类实例
     * @throws IOException io excption
     */
    public <T> T json2Obj(String json, Class<T> t) throws IOException {
        return this.mapper.readValue(json, t);
    }

    /**
     * 将byte数组转换成对象
     * @param <T> 反序列化后的类型
     * @param src 二进制序列
     * @param t 解析目标类
     * @return 反序列化类实例
     * @throws IOException io excption
     */
    public <T> T byte2Obj(byte[] src, Class<T> t) throws IOException {
        return this.mapper.readValue(src, t);
    }

}
