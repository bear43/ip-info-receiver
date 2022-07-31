package com.example.demo.controller;

import com.example.demo.dto.RequestDto;
import com.example.demo.dto.base.PageResponse;
import com.example.demo.filter.RequestFilter;
import com.example.demo.service.RequestService;
import com.example.demo.service.ResponseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/requests")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RequestController {

    RequestService requestService;

    @GetMapping("/all")
    public Flux<PageResponse<RequestDto>> getAll(RequestFilter filter) {
        return Flux.empty();
    }

    @GetMapping("/by-ip/{ip}")
    public Flux<RequestDto> get(@PathVariable String ip) {
        return requestService.getByIp(ip);
    }

    @GetMapping("/by-id/{id}")
    public Mono<RequestDto> get(@PathVariable UUID id) {
        return requestService.getById(id);
    }

    @PostMapping
    public Mono<UUID> create(@RequestBody RequestDto request) {
        return requestService.create(request);
    }

}
