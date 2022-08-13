package com.example.demo.service;

import com.example.demo.dao.request.RequestRepository;
import com.example.demo.dto.RequestDto;
import com.example.demo.dto.base.PageResponse;
import com.example.demo.entity.Request;
import com.example.demo.filter.RequestFilter;
import com.example.demo.util.StringBuilderUtil;
import com.example.demo.util.sql.BinderUtil;
import com.example.demo.util.sql.PaginationUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

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

    public Mono<PageResponse<RequestDto>> filter(RequestFilter filter) {
        return requestRepository.filter(filter);
    }


    private RequestDto toDto(Request request) {
        RequestDto dto = new RequestDto();

        dto.setIp(request.getIp());
        dto.setId(request.getId());

        return dto;
    }
}
