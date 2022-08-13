package com.example.demo.util.sql;

import com.example.demo.filter.RequestFilter;
import com.example.demo.filter.base.PageRequest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

public class PaginationUtil {
    public static StringBuilder applyPagination(StringBuilder stringBuilder) {
        return stringBuilder.append("limit :limit\n")
                .append("offset :offset\n");
    }

    public static DatabaseClient.GenericExecuteSpec applyBindings(
            DatabaseClient.GenericExecuteSpec spec,
            PageRequest pageRequest
    ) {
        return spec.bind("limit", pageRequest.getLimit())
                .bind("offset", (pageRequest.getPage() - 1) * pageRequest.getLimit());
    }
}
