package com.uetty.common.tool.core.json.fastjson2;

import com.alibaba.fastjson2.JSON;

import java.util.List;

/**
 * 弄个工具类，方便升级时统一修改代码
 */
public class FastJsonUtil {


    public static String toJsonString(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static <T> T jsonToObject(String json, Class<T> clz) {
        return JSON.parseObject(json, clz);
    }

    public static <T> List<T> jsonToList(String json, Class<T> clz) {
        return JSON.parseArray(json, clz);
    }
}
