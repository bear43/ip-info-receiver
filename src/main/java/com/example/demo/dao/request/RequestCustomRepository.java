package com.example.demo.dao.request;

import com.example.demo.dto.RequestDto;
import com.example.demo.dto.base.PageResponse;
import com.example.demo.filter.RequestFilter;
import reactor.core.publisher.Mono;

public interface RequestCustomRepository {
    Mono<PageResponse<RequestDto>> filter(RequestFilter filter);
}
