package com.fluxo.report.controller;

import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.dto.SprintReportRequestDto;
import com.fluxo.report.dto.SprintReportResponseDto;
import com.fluxo.report.service.ReportService;
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

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor

@Tag(name = "7. Relatórios", description = "Endpoints para obter relatórios de sprint, andamento e final")
public class ReportController {
    private final ReportService reportService;
    private final SprintReportService sprintReportService;

    @PostMapping("/sprint")
    @Operation(summary = "Criar relatorio de sprint", description = "Cria um relatorio de sprint para o aluno autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Relatorio de sprint criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos ou campos obrigatorios ausentes"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao criar relatorio de sprint")
    })
    public ResponseEntity<SprintReportResponseDto> createSprintReport(
            @Valid @RequestBody SprintReportRequestDto request) {
        SprintReportResponseDto response = sprintReportService.createSprintReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/progress")
    @Operation(summary = "Obter relatorio de andamento", description = "Retorna o relatorio de andamento do aluno autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatorios de andamento obtidos com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao buscar relatorios de andamento")
    })
    public ResponseEntity<List<ProgressReportResponseDto>> getMyProgressReports() {
        return ResponseEntity.ok(reportService.getProgressReports());
    }

    @GetMapping("/me/final")
    @Operation(summary = "Obter relatorios finais", description = "Retorna os relatorios finais do aluno autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatorios finais obtidos com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao buscar relatorios finais")
    })
    public ResponseEntity<List<FinalReportResponseDto>> getMyFinalReports() {
        return ResponseEntity.ok(reportService.getFinalReports());
    }
}
