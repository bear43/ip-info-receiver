package com.example.demo.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LambdaUtil {

    public static <T, U> U ifThen(U root, boolean condition, T object, Consumer<T> action) {
        if (condition) {
            action.accept(object);
        }
        return root;
    }

    public static <T, U> U ifNotNullThen(U root, Supplier<T> getter, Consumer<T> action) {
        T value = getter.get();
        Optional<T> optValue = Optional.ofNullable(value);
        return ifThen(root, optValue.isPresent(), value, action);
    }

    public static <U> U ifNotBlankThen(U root, Supplier<String> getter, Consumer<String> action) {
        String value = getter.get();
        Optional<String> optValue = Optional.ofNullable(value);
        return ifThen(root, !optValue.map(String::isBlank).orElse(true), value, action);
    }

}
