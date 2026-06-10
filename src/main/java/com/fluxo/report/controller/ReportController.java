package com.fluxo.report.controller;

import com.fluxo.report.dto.ConfirmReportUploadRequestDto;
import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.dto.ReportArchiveResponseDto;
import com.fluxo.report.dto.ReportUploadUrlResponseDto;
import com.fluxo.report.dto.SprintReportListResponseDto;
import com.fluxo.report.dto.SprintReportRequestDto;
import com.fluxo.report.dto.SprintReportResponseDto;
import com.fluxo.report.dto.UpdateSprintReportRequestDto;
import com.fluxo.report.dto.UpdateSprintReportResponseDto;
import com.fluxo.report.service.ReportService;
import com.fluxo.report.service.ReportTemplateService;
import com.fluxo.report.service.SprintReportService;
import com.fluxo.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
@Tag(name = "7. Relatórios", description = "Endpoints para obter relatórios de sprint, andamento e final")
public class ReportController {

    private final ReportService reportService;
    private final SprintReportService sprintReportService;
    private final ReportTemplateService reportTemplateService;

    @PostMapping("/progress/upload-url")
    @Operation(summary = "Gerar URL de upload de relatório de andamento", description = "Retorna uma URL temporária para upload direto do PDF no S3")
    public ResponseEntity<ReportUploadUrlResponseDto> createProgressReportUploadUrl(Authentication authentication) {
        User authenticatedUser = (User) authentication.getPrincipal();
        ReportUploadUrlResponseDto response = reportService.createProgressReportUploadUrl(authenticatedUser.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/progress/confirm")
    @Operation(summary = "Confirmar upload de relatório de andamento", description = "Confirma o arquivo enviado ao S3 e registra o relatório do aluno autenticado")
    public ResponseEntity<ReportArchiveResponseDto> confirmProgressReportUpload(
            @Valid @RequestBody ConfirmReportUploadRequestDto request,
            Authentication authentication) {

        User authenticatedUser = (User) authentication.getPrincipal();
        ReportArchiveResponseDto response = reportService.confirmProgressReportUpload(
                request.fileReference(),
                authenticatedUser.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/final/upload-url")
    @Operation(summary = "Gerar URL de upload de relatório final", description = "Retorna uma URL temporária para upload direto do PDF no S3")
    public ResponseEntity<ReportUploadUrlResponseDto> createFinalReportUploadUrl(Authentication authentication) {
        User authenticatedUser = (User) authentication.getPrincipal();
        ReportUploadUrlResponseDto response = reportService.createFinalReportUploadUrl(authenticatedUser.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/final/confirm")
    @Operation(summary = "Confirmar upload de relatório final", description = "Confirma o arquivo enviado ao S3 e registra o relatório do aluno autenticado")
    public ResponseEntity<ReportArchiveResponseDto> confirmFinalReportUpload(
            @Valid @RequestBody ConfirmReportUploadRequestDto request,
            Authentication authentication) {

        User authenticatedUser = (User) authentication.getPrincipal();
        ReportArchiveResponseDto response = reportService.confirmFinalReportUpload(
                request.fileReference(),
                authenticatedUser.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sprint")
    @Operation(summary = "Criar relatório de sprint", description = "Cria um relatório de sprint para o aluno autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Relatório de sprint criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou campos obrigatórios ausentes"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao criar relatório de sprint")
    })
    public ResponseEntity<SprintReportResponseDto> createSprintReport(
            @Valid @RequestBody SprintReportRequestDto request) {
        SprintReportResponseDto response = sprintReportService.createSprintReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me/sprint")
    @Operation(
            summary = "Listar relatórios de sprint",
            description = "Retorna os relatórios de sprint do aluno autenticado, com filtro opcional por projeto"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatórios de sprint retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<List<SprintReportListResponseDto>> getMySprintReports(
            @RequestParam(required = false) Integer projectId
    ) {
        return ResponseEntity.ok(sprintReportService.getMySprintReports(projectId));
    }

    @GetMapping("/me/progress")
    @Operation(summary = "Obter relatório de andamento", description = "Retorna o relatório de andamento do aluno autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "500", description = "Erro interno ao buscar relatórios de andamento")
    })
    public ResponseEntity<List<ProgressReportResponseDto>> getMyProgressReports() {
        return ResponseEntity.ok(reportService.getProgressReports());
    }

    @GetMapping("/me/final")
    @Operation(summary = "Obter relatórios finais", description = "Retorna os relatórios finais do aluno autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatórios finais obtidos com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao buscar relatórios finais")
    })
    public ResponseEntity<List<FinalReportResponseDto>> getMyFinalReports() {
        return ResponseEntity.ok(reportService.getFinalReports());
    }

    @PutMapping("/sprint/{id}")
    @Operation(summary = "Modificar o relatório de sprint", description = "Permite modificar o relatório de sprint do aluno autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos obrigatórios não podem estar vazios"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Relatório não encontrado"),
            @ApiResponse(responseCode = "500", description = "Erro interno ao atualizar relatório de sprint")
    })
    public ResponseEntity<UpdateSprintReportResponseDto> updateSprintReport(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSprintReportRequestDto request) {
        UpdateSprintReportResponseDto updatedSprintReport = sprintReportService.updateSprintReport(id, request);
        return ResponseEntity.ok(updatedSprintReport);
    }

    @GetMapping(value = "/template", produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    @Operation(summary = "Download do template de relatório", description = "Retorna o arquivo DOCX do template de relatório para download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Template não encontrado")
    })
    public ResponseEntity<Resource> downloadReportTemplate() {
        Resource templateFile = reportTemplateService.loadReportTemplate();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-template.docx\"")
                .body(templateFile);
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
