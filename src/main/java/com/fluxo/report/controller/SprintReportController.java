package com.fluxo.report.controller;

import com.fluxo.report.dto.SprintReportRequestDto;
import com.fluxo.report.dto.SprintReportResponseDto;
import com.fluxo.report.service.SprintReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Tag(name = "Relatorios", description = "Endpoints para gerenciamento de relatorios")
public class SprintReportController {

    private final SprintReportService sprintReportService;

    @PostMapping("/sprint")
    @Operation(
            summary = "Criar relatorio de sprint",
            description = "Cria um relatorio de sprint para o aluno autenticado"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Relatorio de sprint criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou campos obrigatorios ausentes"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao criar relatorio de sprint")
    })
    public ResponseEntity<SprintReportResponseDto> createSprintReport(
            @Valid @RequestBody SprintReportRequestDto request
    ) {
        SprintReportResponseDto response = sprintReportService.createSprintReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}