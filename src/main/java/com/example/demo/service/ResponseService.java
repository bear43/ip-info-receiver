package com.example.demo.service;

import com.example.demo.dao.ResponseRepository;
import com.example.demo.dto.RequestDto;
import com.example.demo.dto.ResponseDto;
import com.example.demo.entity.Request;
import com.example.demo.entity.Response;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ResponseService {

    ResponseRepository responseRepository;
    public Mono<ResponseDto> getById(UUID id) {
        return Mono.just(id)
                .flatMap(responseRepository::findByRequestId)
                .map(this::toDto);
    }

    private ResponseDto toDto(Response response) {
        ResponseDto dto = new ResponseDto();

        dto.setId(response.getId());
        dto.setRequestId(response.getRequestId());
        dto.setValue(response.getValue());

        return dto;
    }
}
