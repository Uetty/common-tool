package com.uetty.common.tool.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Maps {

    public static <K, V> Map<K, V> of() {
        return of(HashMap::new);
    }

    public static <K, V> Map<K, V> of(Supplier<Map<K, V>> supplier) {
        return supplier.get();
    }

    public static Map<String, String> of(Supplier<Map<String, String>> supplier, String... keyValues) {
        if (keyValues.length % 2 == 1) {
            throw new RuntimeException("invalid size of array parameter keyValues");
        }
        final Map<String, String> map = supplier.get();
        for (int i = 1; i < keyValues.length; i+=2) {
            map.put(keyValues[i - 1], keyValues[i]);
        }
        return map;
    }

    public static Map<String, String> of(String... keyValues) {
        return of(HashMap::new, keyValues);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> of(Supplier<Map<K, V>> supplier, K key1, V value1, Object... keyValues) {
        if (keyValues.length % 2 == 1) {
            throw new RuntimeException("invalid size of array parameter keyValues");
        }
        if (key1 == null || value1 == null) {
            throw new RuntimeException("parameter key1 and value1 cannot be empty");
        }
        final Class<?> keyClazz = key1.getClass();
        final Class<?> valueClazz = value1.getClass();
        for (int i = 1; i < keyValues.length; i+=2) {
            if (keyValues[i - 1] == null || !keyClazz.isInstance(keyValues[i - 1])) {
                throw new RuntimeException("parameter keyValues[" + (i - 1) + "] is null or type invalid");
            }
            if (keyValues[i] != null && !valueClazz.isInstance(keyValues[i])) {
                throw new RuntimeException("parameter keyValues[" + i + "] type invalid");
            }
        }

        final Map<K, V> map = supplier.get();
        map.put(key1, value1);
        for (int i = 1; i < keyValues.length; i+=2) {
            map.put((K) keyValues[i - 1], (V) keyValues[i]);
        }

        return map;
    }

    public static <K, V> Map<K, V> of(K key1, V value1, Object... keyValues) {
        return of(HashMap::new, key1, value1, keyValues);
    }

    public static <K, V> Map<K, V> of(Supplier<Map<K, V>> supplier, K[] keys, V[] values) {
        if (keys.length != values.length) {
            throw new RuntimeException("size of key must match size of value");
        }

        final Map<K, V> map = supplier.get();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    public static <K, V> Map<K, V> of(K[] keys, V[] values) {
        return of(HashMap::new, keys, values);
    }

    public static <K, V> Map<K, V> of(Supplier<Map<K, V>> supplier, List<K> keys, List<V> values) {
        if (keys.size() != values.size()) {
            throw new RuntimeException("size of key must match size of value");
        }

        final Map<K, V> map = supplier.get();
        for (int i = 0; i < keys.size(); i++) {
            map.put(keys.get(i), values.get(i));
        }

        return map;
    }

    public static <K, V> Map<K, V> of(List<K> keys, List<V> values) {
        return of(HashMap::new, keys, values);
    }

    public static void main(String[] args) {
        final Map<String, Integer> of = of("234", 2, "734", null, "532", 5);
        System.out.println(of);
    }

}
