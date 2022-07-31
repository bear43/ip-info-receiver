package com.example.demo.dao;

import com.example.demo.entity.Response;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ResponseRepository extends ReactiveCrudRepository<Response, UUID> {
    Mono<Response> findByRequestId(UUID requestId);
}
