package com.uetty.common.tool.interfaces;

/**
 * 一些枚举类对应到数据库的值，不是enum.name()也不少enum.ordinal()
 * <p>这里提供一个统一的获取值的方式，枚举类继承该接口可以通过getId()方法获取数据库对应枚举值</p>
 */
public interface Identifiable {

    String getId();

    static <T extends Enum<T> & Identifiable> T getEnumConstant(String id, Class<T> clz) {
        if (id == null) {
            return null;
        }

        T[] values = clz.getEnumConstants();

        for (T item : values) {
            if (id.equalsIgnoreCase(item.getId())) {
                return item;
            }
        }
        return null;
    }
}
