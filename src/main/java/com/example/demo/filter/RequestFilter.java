package com.example.demo.filter;

import com.example.demo.filter.base.PageRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestFilter extends PageRequest {
    UUID id;
    String ip;
}
