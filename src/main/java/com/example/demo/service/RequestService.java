package com.example.demo.service;

import com.example.demo.dao.RequestRepository;
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
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.util.Pair;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

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
        return Mono.just(getSql("select *", filter))
                .map(PaginationUtil::applyPagination)
                .flatMap(sb -> generateSqlAndApplyBindings(sb, filter))
                .map(spec -> PaginationUtil.applyBindings(spec, filter))
                .map(DatabaseClient.GenericExecuteSpec::fetch)
                .flux()
                .flatMap(RowsFetchSpec::all)
                .map(this::fromFetchSpecToDto)
                .collectList()
                .map(l -> getRequestDtoPageResponse(filter, l))
                .zipWith(getCount(filter), PageResponse::setTotal);
    }

    private StringBuilder getSql(String select, RequestFilter filter) {
        StringBuilder sb = new StringBuilder()
                .append(select)
                .append(" from request r\n")
                .append("where 1=1\n");
        StringBuilderUtil.ifNotNullThenAppend(sb, filter::getId, "and r.id = :id\n");
        StringBuilderUtil.ifNotBlankThenAppend(sb, filter::getIp, "and r.ip like '%' || :ip || '%'\n");
        return sb;
    }

    private Mono<DatabaseClient.GenericExecuteSpec> generateSqlAndApplyBindings(StringBuilder sb, RequestFilter filter) {
        return Mono.just(sb.toString())
                .map(databaseClient::sql)
                .map(spec -> BinderUtil.ifNotNullThen(spec, "id", filter::getId))
                .map(spec -> BinderUtil.ifNotNullThen(spec, "ip", filter::getIp));
    }

    private Mono<Long> getCount(RequestFilter filter) {
        return Mono.just(getSql("select count(1) as cnt", filter))
                .flatMap(sb -> generateSqlAndApplyBindings(sb, filter))
                .map(DatabaseClient.GenericExecuteSpec::fetch)
                .flatMap(FetchSpec::one)
                .map(map -> (Long) map.get("cnt"));
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
