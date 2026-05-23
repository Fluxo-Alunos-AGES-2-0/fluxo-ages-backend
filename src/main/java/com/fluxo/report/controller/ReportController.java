package com.fluxo.report.controller;

import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.dto.ReportArchiveResponseDto;
import com.fluxo.report.dto.SprintReportRequestDto;
import com.fluxo.report.dto.SprintReportResponseDto;
import com.fluxo.report.dto.SprintReportListResponseDto;
import com.fluxo.report.service.ReportService;
import com.fluxo.report.service.SprintReportService;
import com.fluxo.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Tag(name = "7. Relatórios", description = "Endpoints para obter relatórios de sprint, andamento e final")
public class ReportController {

    private final ReportService reportService;
    private final SprintReportService sprintReportService;

    @PostMapping(value = "/progress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de relatório de andamento", description = "Faz o upload do arquivo de relatório de andamento do aluno autenticado")
    public ResponseEntity<ReportArchiveResponseDto> uploadProgressReport(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User authenticatedUser = (User) authentication.getPrincipal();
        Integer studentId = authenticatedUser.getId();
        ReportArchiveResponseDto response = reportService.uploadProgressReport(file, studentId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/final", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de relatório final", description = "Faz o upload do arquivo de relatório final do aluno autenticado")
    public ResponseEntity<ReportArchiveResponseDto> uploadFinalReport(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User authenticatedUser = (User) authentication.getPrincipal();
        Integer studentId = authenticatedUser.getId();
        ReportArchiveResponseDto response = reportService.uploadFinalReport(file, studentId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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

    @GetMapping("/me/sprint")
    @Operation(
            summary = "Listar relatorios de sprint",
            description = "Retorna os relatorios de sprint do aluno autenticado, com filtro opcional por projeto"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatorios de sprint retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuario nao autenticado")
    })
    public ResponseEntity<List<SprintReportListResponseDto>> getMySprintReports(
            @RequestParam(required = false) Integer projectId
    ) {
        return ResponseEntity.ok(sprintReportService.getMySprintReports(projectId));
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

    @DeleteMapping("/{idReport}")
    @Operation(
            summary = "Excluir relatório",
            description = "Exclui um relatório do aluno autenticado, removendo também seus registros especializados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Relatório excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para excluir este relatório"),
            @ApiResponse(responseCode = "404", description = "Relatório não encontrado")
    })
    public ResponseEntity<Void> deleteReport(@PathVariable Integer idReport) {
        reportService.deleteReport(idReport);
        return ResponseEntity.noContent().build();
    }
}