package com.fluxo.report.controller;

import com.fluxo.report.dto.HoursReportDto;
import com.fluxo.report.service.HoursReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "5. Relatórios", description = "Endpoints para relatórios de horas do estudante")
public class ReportController {

    private final HoursReportService hoursReportService;

    @GetMapping("/me/hours")
    @Operation(
            summary = "Obter histórico de horas do estudante autenticado",
            description = "Retorna todos os registros de horas submetidos pelo estudante autenticado, independentemente do status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico de horas retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<List<HoursReportDto>> getMyHours(
            @RequestParam(value = "id_report", required = false) Integer idReport
    ) {
        return ResponseEntity.ok(hoursReportService.getMyReports(idReport));
    }
}
