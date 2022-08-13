package com.example.demo.util;

import java.util.function.Supplier;

public class StringBuilderUtil {
    public static <T, V> StringBuilder ifNotNullThenAppend(StringBuilder sb, Supplier<T> getter, V value) {
        return LambdaUtil.ifNotNullThen(sb, getter, (getterValue) -> {
            sb.append(value);
        });
    }

    public static <V> StringBuilder ifNotBlankThenAppend(StringBuilder sb, Supplier<String> getter, V value) {
        return LambdaUtil.ifNotNullThen(sb, getter, (getterValue) -> {
            sb.append(value);
        });
    }
}
