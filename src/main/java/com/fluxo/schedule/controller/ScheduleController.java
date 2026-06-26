package com.fluxo.schedule.controller;

import com.fluxo.schedule.dto.ScheduleResponseDTO;
import com.fluxo.schedule.entity.Schedule;
import com.fluxo.schedule.entity.SchedulePeriod;
import com.fluxo.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@Tag(name = "5. Agendamentos", description = "Endpoints para gerenciamento de agendamentos e horarios")
public class ScheduleController {

    private static final DateTimeFormatter EVENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/schedule")
    @Operation(summary = "Listar agendamentos", description = "Retorna uma lista de horarios/agendamentos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parametro diaTurno ausente ou invalido")
    })
    public ResponseEntity<?> listSchedule(@RequestParam(value = "diaTurno", required = false) String diaTurno) {
        SchedulePeriod schedulePeriod = SchedulePeriod.fromValue(diaTurno)
                .orElse(null);

        if (schedulePeriod == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Parametro diaTurno e obrigatorio e deve ser valido"));
        }

        List<ScheduleResponseDTO> events = scheduleService.listScheduleByPeriod(schedulePeriod.getValue())
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(events);
    }

    private ScheduleResponseDTO toResponse(Schedule schedule) {
        return new ScheduleResponseDTO(
                schedule.getId(),
                schedule.getEvent(),
                schedule.getEventDate().toString(),
                formatEventTime(schedule.getEventTime()),
                schedule.getEventPeriod()
        );
    }

    private String formatEventTime(LocalTime eventTime) {
        return eventTime.format(EVENT_TIME_FORMATTER);
    }
}
