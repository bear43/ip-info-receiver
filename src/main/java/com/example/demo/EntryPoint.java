package com.example.demo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "Reactive test application",
				description = "Receive info about requested IP from ipwhois service",
				version = "1"
		)
)
public class EntryPoint {

	public static void main(String[] args) {
		SpringApplication.run(EntryPoint.class, args);
	}

}
