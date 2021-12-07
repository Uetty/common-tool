package com.uetty.common.tool.interfaces;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 获取函数实现名辅助接口
 * @author vince
 */
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {


}
