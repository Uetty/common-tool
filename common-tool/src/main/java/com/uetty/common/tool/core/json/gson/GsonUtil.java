package com.uetty.common.tool.core.json.gson;

import com.google.gson.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GsonUtil {

	public static String toJson(Object a, Consumer<GsonBuilder> configSetter) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		if (configSetter != null) {
			configSetter.accept(gsonBuilder);
		}
		Gson gson = gsonBuilder.create();
		return gson.toJson(a);
	}

	public static String toJson(Object a) {
		return new Gson().toJson(a).toString();
	}
	
	public static <T> List<T> fromJsonArray(String jsonArr, Class<T> clz) {
		//Json的解析类对象
	    JsonParser parser = new JsonParser();
	    //将JSON的String 转成一个JsonArray对象
	    JsonArray jsonArray = parser.parse(jsonArr).getAsJsonArray();

	    Gson gson = new Gson();
	    List<T> list = new ArrayList<T>();
	    //加强for循环遍历JsonArray
	    for (JsonElement user : jsonArray) {
	        //使用GSON，直接转成Bean对象
	        T t = gson.fromJson(user, clz);
	        list.add(t);
	    }
	    return list;
	}
	
	public static <T> T fromJsonObject(String jsonObj, Class<T> clz) {
		return new Gson().fromJson(jsonObj, clz);
	}
}
