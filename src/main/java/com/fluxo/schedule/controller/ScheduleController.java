package com.fluxo.schedule.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import com.fluxo.schedule.dto.ScheduleEventDto;
import com.fluxo.schedule.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Tag(name = "5. Agendamentos", description = "Endpoints para gerenciamento de agendamentos e horários")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/schedule")
    @Operation(summary = "Listar agendamentos", description = "Retorna uma lista de horários/agendamentos para um dia/turno específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro diaTurno ausente ou inválido")
    })
    public ResponseEntity<List<ScheduleEventDto>> listSchedule(
            @RequestParam(name = "diaTurno") String diaTurno,
            @RequestParam(name = "sprint", required = false) Integer sprint
    ) {
        if (diaTurno == null || diaTurno.isBlank() || !diaTurno.contains("_")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter diaTurno is required and must follow format TURNO_DIA, e.g. LM_SEGQUA");
        }

        List<ScheduleEventDto> events = scheduleService.listByDiaTurnoAndOptionalSprint(diaTurno, sprint);
        return ResponseEntity.ok(events);
    }
}
