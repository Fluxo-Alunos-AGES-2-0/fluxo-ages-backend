package com.fluxo.hours.controller;

import com.fluxo.hours.config.OpenHoursReminderProperties;
import com.fluxo.hours.dto.*;
import com.fluxo.hours.entity.HoursReport;
import com.fluxo.hours.service.HoursReportService;
import com.fluxo.hours.service.HoursReminderService;
import com.fluxo.hours.service.HoursService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/hours")
@RequiredArgsConstructor
@Tag(name = "4. Horas", description = "Endpoints para registro e gerenciamento de horas")
public class HoursController {

    private final HoursService hoursService;
    private final HoursReportService hoursReportService;
    private final HoursReminderService hoursReminderService;
    private final OpenHoursReminderProperties openHoursReminderProperties;

    @PostMapping("/start")
    @Operation(summary = "Iniciar registro de hora", description = "Inicia a contagem de tempo de trabalho (check-in)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Horario iniciado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Ja existe uma contagem de tempo ativa")
    })
    public ResponseEntity<StartHoursResponseDto> startHours() {
        StartHoursResponseDto response = hoursService.startHours();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/stop")
    @Operation(summary = "Parar registro de hora", description = "Para a contagem de tempo de trabalho (check-out)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horario parado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nenhum registro ativo para ser parado ou dados invalidos")
    })
    public ResponseEntity<StopHoursResponseDto> stopHours(
            @Valid @RequestBody StopHoursRequestDto request
    ) {
        StopHoursResponseDto response = hoursService.stopHours(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Obter registro ativo", description = "Recupera detalhes da contagem de tempo ativa no momento, se houver")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Registro ativo encontrado"),
        @ApiResponse(responseCode = "404", description = "Nenhum registro ativo encontrado")
    })
    public ResponseEntity<ActiveHoursResponseDto> getActiveHours() {
        return hoursService.findActiveHours()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    @Operation(
            summary = "Obter historico de horas do estudante autenticado",
            description = "Retorna todos os registros de horas submetidos pelo estudante autenticado, independentemente do status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historico de horas retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Nao autorizado")
    })
    public ResponseEntity<List<HoursReportDto>> getMyHours(
            @RequestParam(value = "id_project", required = false) Integer idProject
    ) {
        return ResponseEntity.ok(hoursReportService.getMyHours(idProject));
    }

    @GetMapping("/me/control")
    @Operation(summary = "Obter informacoes de controle de horas", description = "Retorna informacoes necessarias para o controle de Horas (Concluidas, A cumprir, Total e Percentual).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total de horas retornado com sucesso")
    })
    public ResponseEntity<HoursDTO> getHourControl() {
        return ResponseEntity.ok(hoursService.getHourControl());
    }

    @PutMapping("/report/{id}")
    @Operation(summary = "Modificar o relatório de horas", description = "Permitir modificar o relatório de horas do aluno autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório alterado com sucesso!"),
            @ApiResponse(responseCode = "403", description = "Usuario nao autenticado"),
            @ApiResponse(responseCode = "404", description = "Relatorio não encontrado")
    })
    public ResponseEntity<UpdateHoursReportResponseDto> updateHoursReport(@PathVariable Integer id, @Valid @RequestBody UpdateHoursReportRequestDto request) {
        UpdateHoursReportResponseDto updatedSprintReport = hoursReportService.updateHoursReport(id, request);
        return ResponseEntity.ok(updatedSprintReport);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoursReport> getHourReport(@PathVariable Integer id){
        return ResponseEntity.ok(hoursReportService.getHoursReportById(id));
    }

    @PostMapping("/reminders/open/dispatch")
    @Operation(
            summary = "Disparar lembretes de horas abertas",
            description = "Endpoint interno para envio de lembretes de horas abertas dentro da janela configurada."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lembretes processados com sucesso"),
        @ApiResponse(responseCode = "403", description = "Segredo interno invalido"),
        @ApiResponse(responseCode = "503", description = "Feature desabilitada")
    })
    public ResponseEntity<OpenHoursReminderDispatchResponseDto> dispatchOpenHoursReminders(
            @RequestHeader("X-Internal-Secret") String internalSecret
    ) {
        validateReminderDispatchRequest(internalSecret);

        OpenHoursReminderDispatchResponseDto response = hoursReminderService.dispatchOpenHoursReminderEmails(
                openHoursReminderProperties.reminderAge(),
                openHoursReminderProperties.reminderWindow()
        );

        return ResponseEntity.ok(response);
    }

    private void validateReminderDispatchRequest(String internalSecret) {
        if (!openHoursReminderProperties.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Feature de lembrete desabilitada");
        }

        if (openHoursReminderProperties.getSecret().isBlank()
                || !openHoursReminderProperties.getSecret().equals(internalSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Segredo interno invalido");
        }
    }
}
