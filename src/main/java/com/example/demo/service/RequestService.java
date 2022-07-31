package com.example.demo.service;

import com.example.demo.dao.RequestRepository;
import com.example.demo.dto.RequestDto;
import com.example.demo.entity.Request;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestService {

    RequestRepository requestRepository;

    public Mono<UUID> create(RequestDto request) {
        return Mono.just(new Request())
                .zipWith(Mono.just(request.getIp()), Request::setIp)
                .flatMap(requestRepository::save)
                .map(Request::getId);
    }

    public Flux<RequestDto> getByIp(String ip) {
        return Flux.just(ip)
                .flatMap(requestRepository::findAllByIp)
                .map(this::toDto);
    }

    public Mono<RequestDto> getById(UUID id) {
        return Mono.just(id)
                .flatMap(requestRepository::findById)
                .map(this::toDto);
    }

    private RequestDto toDto(Request request) {
        RequestDto dto = new RequestDto();

        dto.setIp(request.getIp());
        dto.setId(request.getId());

        return dto;
    }
}
