package com.fluxo.dashboard.controller;

import com.fluxo.dashboard.dto.DashboardResponseDTO;
import com.fluxo.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "3. Dashboard", description = "Endpoints para obter dados para a home do usuário (dashboard)")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Obter dados do estudante", description = "Retorna os dados necessários para exibir na tela inicial (dashboard) do usuário")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard gerado com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<DashboardResponseDTO> getDashboard(
            @AuthenticationPrincipal(expression = "id") Integer userId
    ) {
        return ResponseEntity.ok(dashboardService.getDashboard(userId));
    }
}
