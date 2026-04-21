package com.fluxo.hours.controller;

import com.fluxo.hours.dto.ActiveHoursResponseDto;
import com.fluxo.hours.dto.HoursDTO;
import com.fluxo.hours.dto.StartHoursResponseDto;
import com.fluxo.hours.dto.StopHoursRequestDto;
import com.fluxo.hours.dto.StopHoursResponseDto;
import com.fluxo.hours.service.HoursService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hours")
@RequiredArgsConstructor
@Tag(name = "4. Horas", description = "Endpoints para registro e gerenciamento de horas")
public class HoursController {

    private final HoursService hoursService;

    @PostMapping("/start")
    @Operation(summary = "Iniciar registro de hora", description = "Inicia a contagem de tempo de trabalho (check-in)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Horário iniciado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Já existe uma contagem de tempo ativa")
    })
    public ResponseEntity<StartHoursResponseDto> startHours() {
        StartHoursResponseDto response = hoursService.startHours();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/stop")
    @Operation(summary = "Parar registro de hora", description = "Para a contagem de tempo de trabalho (check-out)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horário parado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Nenhum registro ativo para ser parado ou dados inválidos")
    })
    public ResponseEntity<StopHoursResponseDto> stopHours(
            @RequestBody StopHoursRequestDto request
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
    @Operation(summary = "Obter minhas horas", description = "Retorna a lista do histórico de horas resgistradas pelo usuário autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso")
    })
    public ResponseEntity<List<StopHoursResponseDto>> getMyHours() {
        return ResponseEntity.ok(hoursService.getMyHours());
    }

    @GetMapping("/me/control")
    @Operation(summary = "Obter informações de controle de horas", description = "Retorna informações necessárias para o controle de Horas (Concluídas, A cumprir, Total e Percentual).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total de horas retornado com sucesso")
    })
    public ResponseEntity<HoursDTO> getHourControl() {
        return ResponseEntity.ok(hoursService.getHourControl());
    }
}
