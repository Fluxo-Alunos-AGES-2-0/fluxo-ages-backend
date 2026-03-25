package com.fluxo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String check() {
        return "🚀 API Fluxo 2.0 está ON e rodando no Docker!";
    }
}