package com.example.demo.controller;

import com.example.demo.dto.ResponseDto;
import com.example.demo.service.ResponseService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/responses")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ResponseController {

    ResponseService responseService;

    @GetMapping("/by-id/{id}")
    public Mono<ResponseDto> getById(@PathVariable String id) {
        return Mono.empty();
    }


    @GetMapping("/by-ip/{ip}")
    public Flux<ResponseDto> getByIp(@PathVariable String ip) {
        return Flux.empty();
    }
}
