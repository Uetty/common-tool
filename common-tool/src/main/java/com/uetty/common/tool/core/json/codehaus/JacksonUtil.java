package com.uetty.common.tool.core.json.codehaus;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.*;

import java.io.IOException;

/** 
 * codehaus版本jackson工具类（旧版jackson，新版是fasterxml）
 * @author vince
 */
public class JacksonUtil {

	private ObjectMapper mapper = new ObjectMapper();
	
    /**
     * 反序列化时，float转为成BigDecimal
     */
    public JacksonUtil withBigDecimalForFloats() {
        this.mapper.configure(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS, true);
        return this;
    }

    /**
     * 反序列化时，空字符串转为null
     * <p>这里容易理解有歧义，java类属性不是String的时候会出现null，否则还是空字符串
     */
    public JacksonUtil withEmptyStringAsNull() {
    	this.mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        return this;
    }
    
    /**
     * 序列化时，忽略Map中的空值条目
     */
    public JacksonUtil withDisWriteNullMapValue() {
    	this.mapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
    	return this;
    }

    /**
     * 反序列化时，java基本类型不接受空值
     */
    public JacksonUtil withDisAcceptNullPrimitive() {
    	this.mapper.configure(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
    	return this;
    }

    /**
     * 反序列化时，非数组值能够被强转为数组
     */
    public JacksonUtil withSingleValueAsArray() {
        this.mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return this;
    }
    

    /**
     * 反序列化时，忽略未知属性
     */
    public JacksonUtil withDisFailUnknow() {
    	this.mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return this;
    }


    /**
     * 序列化时，缩进输出字符串
     * <p>缩进输出，即格式化后输出，更利于查看
     */
    public JacksonUtil withIndentOutput() {
    	this.mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        return this;
    }

    /**
     * 序列化时，时间不转为时间戳
     */
    public JacksonUtil withDisDateToTimestamp() {
    	this.mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        return this;
    }

    /**
     * 序列化时，属性输出按字母顺序排列
     */
    public JacksonUtil withSortFieldNames() {
    	this.mapper.configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true);
        return this;
    }

    /**
     *  序列化时，不能处理的类型，赋值null
     */
    public JacksonUtil withIgnoreEmptyBeans() {
    	this.mapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        return this;
    }

    /**
     * 序列化/反序列化时，不重新包装序列化时捕获到的异常，而直接抛出
     * @since 1.7
     */
    public JacksonUtil withDisWrapExceptions() {
    	this.mapper.configure(SerializationConfig.Feature.WRAP_EXCEPTIONS, false);
    	this.mapper.configure(DeserializationConfig.Feature.WRAP_EXCEPTIONS, false);
        return this;
    }

    /**
     * 序列化/反序列化时，使用toString()方法序列化/反序列化枚举类型（未设置时，默认使用name()方法）
     */
    public JacksonUtil withEnumToString() {
    	this.mapper.configure(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING, true);
    	this.mapper.configure(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING, true);
        return this;
    }

    /**
     * 自定义类型转换的支持
     */
    public JacksonUtil registerModule(Module module) {
    	this.mapper.registerModule(module);
    	return this;
    }
    
    /**
     * 将Object对象转化成json字符串
     */
    public String obj2Json(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
        return this.mapper.writeValueAsString(obj);
    }

    /**
     * 将Object对象转化成byte数组
     */
    public byte[] obj2Byte(Object obj) throws JsonGenerationException, JsonMappingException, IOException {
        return this.mapper.writeValueAsBytes(obj);
    }

    /**
     * 将json字符串转化成bean对象
     */
    public <T> T json2Obj(String json, Class<T> t) throws JsonParseException, JsonMappingException, IOException {
        return this.mapper.readValue(json, t);
    }

    /**
     * 将byte数组转换成对象
     */
    public <T> T byte2Obj(byte[] src, Class<T> t) throws JsonParseException, JsonMappingException, IOException {
        return this.mapper.readValue(src, t);
    }

    
}
