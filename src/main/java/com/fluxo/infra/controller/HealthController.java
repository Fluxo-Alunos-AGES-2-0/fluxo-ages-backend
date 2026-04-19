package com.fluxo.infra.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "6. Saúde do Sistema", description = "Endpoints para verificação de status e saúde da API")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Verificar saúde da API", description = "Retorna uma mensagem de status garantindo que o servidor está rodando")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API está online")
    })
    public String check() {
        return "🚀 API Fluxo 2.0 está ON e rodando no Docker!";
    }
}