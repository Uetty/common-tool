package com.uetty.common.tool.interfaces;

import java.util.Objects;

public interface IntIdentifiable {

    Integer getId();

    static <T extends Enum<T> & IntIdentifiable> T getEnumConstant(Integer id, Class<T> clz) {
        if (id == null) {
            return null;
        }

        T[] values = clz.getEnumConstants();

        for (T item : values) {
            if (Objects.equals(item.getId(), id)) {
                return item;
            }
        }
        return null;
    }
}
