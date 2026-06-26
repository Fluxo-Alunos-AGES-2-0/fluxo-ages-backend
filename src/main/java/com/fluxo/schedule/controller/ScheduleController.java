package com.fluxo.schedule.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "5. Agendamentos", description = "Endpoints para gerenciamento de agendamentos e horários")
public class ScheduleController {

    private final com.fluxo.schedule.service.ScheduleService scheduleService;

    public ScheduleController(com.fluxo.schedule.service.ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/schedule")
    @Operation(summary = "Listar agendamentos", description = "Retorna uma lista de horários/agendamentos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro diaTurno ausente ou inválido")
    })
    public ResponseEntity<?> listSchedule(
            @org.springframework.web.bind.annotation.RequestParam(value = "diaTurno", required = false) String diaTurno) {
        
        if (diaTurno == null || diaTurno.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Parâmetro diaTurno é obrigatório e não pode ser vazio"));
        }

        List<com.fluxo.schedule.entity.Schedule> scheduleList = scheduleService.listScheduleByPeriod(diaTurno);
        
        List<com.fluxo.schedule.dto.ScheduleResponseDTO> events = scheduleList.stream()
            .map(s -> new com.fluxo.schedule.dto.ScheduleResponseDTO(
                    s.getId(),
                    s.getEvent(),
                    s.getEventDate().toString(),
                    s.getEventTime().toString(),
                    s.getEventPeriod()
            )).toList();

        return ResponseEntity.ok(events);
    }
}
