package com.example.demo.config;

import com.example.demo.entity.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
public class JpaConfig {
    @Bean
    BeforeConvertCallback<Request> idGeneratingCallback(DatabaseClient databaseClient) {

        return (request, sqlIdentifier) -> Mono.just(request)
                .map((instance) -> {
                    if (instance.getId() == null) {
                        instance.setId(UUID.randomUUID());
                    }
                    return instance;
                });
    }
}
