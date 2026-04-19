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

    @GetMapping("/schedule")
    @Operation(summary = "Listar agendamentos", description = "Retorna uma lista de horários/agendamentos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    })
    public ResponseEntity<List<Map<String, String>>> listSchedule() {
        return ResponseEntity.ok(List.of());
    }
}
