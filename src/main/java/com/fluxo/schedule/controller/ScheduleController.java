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
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Parâmetro diaTurno ausente ou inválido")
    })
    public ResponseEntity<?> listSchedule(
            @org.springframework.web.bind.annotation.RequestParam(value = "diaTurno", required = false) String diaTurno) {
        
        if (diaTurno == null || diaTurno.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Parâmetro diaTurno é obrigatório e não pode ser vazio"));
        }

        List<com.fluxo.schedule.dto.ScheduleResponseDTO> events = List.of(
            new com.fluxo.schedule.dto.ScheduleResponseDTO(1, "Apresentação da Sprint 3 para stakeholders e planning da Sprint 4", "2025-06-05", "19:00 - 22:30", diaTurno),
            new com.fluxo.schedule.dto.ScheduleResponseDTO(2, "Retrospectiva da Sprint 3 + Entrega do Relatório da Sprint 3 no Fluxo AGES", "2025-06-05", "19:00 - 22:30", diaTurno),
            new com.fluxo.schedule.dto.ScheduleResponseDTO(3, "Desenvolvimento da Sprint 4", "2025-06-12", "19:00 - 22:30", diaTurno),
            new com.fluxo.schedule.dto.ScheduleResponseDTO(4, "Sem aula, VAI BRASIL!!!", "2025-06-19", "19:00 - 22:30", diaTurno),
            new com.fluxo.schedule.dto.ScheduleResponseDTO(5, "Entrega FINAL do Projeto + Retrospectiva do Projeto e o mais importante: PIZZA", "2025-06-26", "19:00 - 22:30", diaTurno),
            new com.fluxo.schedule.dto.ScheduleResponseDTO(6, "Apresentação dos Projetos AGES para todos os times + Escolha do projeto destaque", "2025-07-03", "19:00 - 22:30", diaTurno)
        );

        return ResponseEntity.ok(events);
    }
}
