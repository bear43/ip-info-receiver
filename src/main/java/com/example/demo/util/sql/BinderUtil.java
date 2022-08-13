package com.example.demo.util.sql;

import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Supplier;

public class BinderUtil {
    public static DatabaseClient.GenericExecuteSpec ifNotNullThen(
            DatabaseClient.GenericExecuteSpec spec,
            String key,
            Supplier<?> getter
            ) {
        return Optional.ofNullable(getter.get())
                .map(value -> spec.bind(key, value))
                .orElse(spec);
    }

    public static DatabaseClient.GenericExecuteSpec ifNotEmptyThen(
            DatabaseClient.GenericExecuteSpec spec,
            String key,
            Supplier<String> getter
    ) {
        return Optional.ofNullable(getter.get())
                .filter(string -> !string.isBlank())
                .map(value -> spec.bind(key, value))
                .orElse(spec);
    }
}
