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
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestService {

    RequestRepository requestRepository;
    DatabaseClient databaseClient;

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
                .map(tuple -> {
                    DatabaseClient.GenericExecuteSpec baseSpec = tuple.getT1();
                    DatabaseClient.GenericExecuteSpec pageSpec = tuple.getT2();
                    if (filter.getId() != null) {
                        baseSpec = baseSpec.bind("id", filter.getId());
                        pageSpec = pageSpec.bind("id", filter.getId());
                    }
                    if (filter.getIp() != null) {
                        baseSpec = baseSpec.bind("ip", filter.getIp());
                        pageSpec = pageSpec.bind("ip", filter.getIp());
                    }
                    Integer limit = filter.getLimit();
                    Integer page = filter.getPage();
                    baseSpec = baseSpec.bind("limit", limit);
                    baseSpec = baseSpec.bind("offset", (page - 1) * limit);
                    return Tuples.of(baseSpec.fetch(), pageSpec.fetch());
                })
                .flatMap(tuple -> {
                    Flux<RequestDto> result = tuple.getT1()
                                    .all()
                                    .map(this::fromFetchSpecToDto);
                    Mono<Long> cnt = tuple.getT2()
                            .one()
                            .map(map -> (Long) map.get("cnt"));
                    return result.collectList()
                            .map(l -> new PageResponse<RequestDto>().setData(l)
                                    .setPage(filter.getPage())
                                    .setLimit(filter.getLimit())
                            )
                            .zipWith(cnt, PageResponse::setTotal);
                });
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
