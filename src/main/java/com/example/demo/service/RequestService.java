package com.example.demo.service;

import com.example.demo.dao.RequestRepository;
import com.example.demo.dto.RequestDto;
import com.example.demo.dto.base.PageResponse;
import com.example.demo.entity.Request;
import com.example.demo.filter.RequestFilter;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.util.Pair;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.r2dbc.core.PreparedOperation;
import org.springframework.r2dbc.core.QueryOperation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestService {

    RequestRepository requestRepository;
    DatabaseClient databaseClient;

    R2dbcEntityTemplate r2dbcEntityTemplate;

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
        return Mono.just(
                        new StringBuilder()
                                .append(" from request r\n")
                                .append("where 1=1\n")
                )
                .map(sb -> {
                    if (filter.getId() != null) {
                        sb.append("and r.id = :id\n");
                    }
                    if (filter.getIp() != null) {
                        sb.append("and r.ip like '%' || :ip || '%'\n");
                    }
                    return sb;
                })
                .zipWith(Mono.just(new StringBuilder()), (baseSql, sb) -> {
                    sb.append(baseSql)
                            .insert(0, "select count(1) as cnt");

                    baseSql.insert(0, "select *")
                            .append("limit :limit\n")
                            .append("offset :offset\n");

                    return Tuples.of(baseSql, sb);
                })
                .map(tuple -> Tuples.of(tuple.getT1().toString(), tuple.getT2().toString()))
                .map(tuple -> Tuples.of(databaseClient.sql(tuple.getT1()), databaseClient.sql(tuple.getT2())))
                .flatMap(tuple -> Flux.just(
                                        Pair.of("id", Optional.ofNullable(filter.getId())),
                                        Pair.of("ip", Optional.ofNullable(filter.getIp())),
                                        Pair.of("limit", Optional.ofNullable(filter.getLimit())),
                                        Pair.of(
                                                "offset",
                                                Optional.of(
                                                        (Optional.ofNullable(filter.getPage()).orElse(1) - 1) *
                                                                Optional.ofNullable(filter.getLimit()).orElse(0)
                                                )
                                        )
                                )
                        .filter(param -> param.getSecond().isPresent())
                        .map(param -> Pair.of(param.getFirst(), param.getSecond().get()))
                        .collectList()
                        .map(params -> {
                                    DatabaseClient.GenericExecuteSpec baseSpec = tuple.getT1();
                                    for (Pair<String, ? extends Serializable> param : params) {
                                        baseSpec = baseSpec.bind(param.getFirst(), param.getSecond());
                                    }

                                    DatabaseClient.GenericExecuteSpec pageSpec = tuple.getT2();
                                    List<Pair<String, ? extends Serializable>> paginationParams = params.stream()
                                            .filter(param ->
                                                    !(param.getFirst().equals("limit") ||
                                                            param.getFirst().equals("offset"))
                                            ).collect(Collectors.toList());

                                    for (Pair<String, ? extends Serializable> param : paginationParams) {
                                        pageSpec = pageSpec.bind(param.getFirst(), param.getSecond());
                                    }
                                    return Tuples.of(baseSpec.fetch(), pageSpec.fetch());
                                })
                )
                .flatMap(tuple -> {
                    Mono<List<RequestDto>> result = tuple.getT1()
                            .all()
                            .map(this::fromFetchSpecToDto)
                            .collectList();

                    Mono<Long> cnt = tuple.getT2()
                            .one()
                            .map(map -> (Long) map.get("cnt"));

                    return result.map(l -> getRequestDtoPageResponse(filter, l))
                            .zipWith(cnt, PageResponse::setTotal);
                });
    }

    private static PageResponse<RequestDto> getRequestDtoPageResponse(RequestFilter filter, List<RequestDto> l) {
        return new PageResponse<RequestDto>().setData(l)
                .setPage(filter.getPage())
                .setLimit(filter.getLimit());
    }

    private RequestDto fromFetchSpecToDto(Map<String, Object> map) {
        RequestDto dto = new RequestDto();

        dto.setId((UUID) map.get("id"));
        dto.setIp((String) map.get("ip"));

        return dto;
    }

    private RequestDto toDto(Request request) {
        RequestDto dto = new RequestDto();

        dto.setIp(request.getIp());
        dto.setId(request.getId());

        return dto;
    }
}
