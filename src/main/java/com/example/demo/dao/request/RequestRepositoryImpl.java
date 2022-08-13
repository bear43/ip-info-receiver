package com.example.demo.dao.request;

import com.example.demo.dto.RequestDto;
import com.example.demo.dto.base.PageResponse;
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
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RequestRepositoryImpl implements RequestCustomRepository {

    DatabaseClient databaseClient;

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
}
