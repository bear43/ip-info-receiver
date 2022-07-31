package com.example.demo.dao;

import com.example.demo.entity.Request;
import org.reactivestreams.Publisher;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface RequestRepository extends ReactiveCrudRepository<Request, UUID> {
    Flux<Request> findAllByIp(String ip);
}
