package com.example.demo.dao.request;

import com.example.demo.entity.Request;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface RequestRepository extends ReactiveCrudRepository<Request, UUID>, RequestCustomRepository {
    Flux<Request> findAllByIp(String ip);
}
