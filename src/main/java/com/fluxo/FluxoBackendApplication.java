package com.fluxo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Não esqueça deste import!

import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FluxoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FluxoBackendApplication.class, args);
    }
}