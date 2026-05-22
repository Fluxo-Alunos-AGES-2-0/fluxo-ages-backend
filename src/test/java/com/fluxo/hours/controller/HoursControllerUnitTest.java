package com.fluxo.hours.controller;

import com.fluxo.hours.dto.*;
import com.fluxo.hours.exception.ActiveHoursNotFoundException;
import com.fluxo.hours.exception.HoursAlreadyOpenException;
import com.fluxo.hours.service.HoursReportService;
import com.fluxo.hours.service.HoursService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HoursController Unit Tests")
class HoursControllerUnitTest {

    @InjectMocks
    private HoursController hoursController;

    @Mock
    private HoursService hoursService;
    @Mock
    private HoursReportService hoursReportService;

    @Test
    @DisplayName("Should return 200 when active hours exists")
    void shouldReturn200WhenActiveHoursExists() {

        ActiveHoursResponseDto dto = new ActiveHoursResponseDto(1, Instant.now());

        when(hoursService.findActiveHours()).thenReturn(Optional.of(dto));

        ResponseEntity<ActiveHoursResponseDto> response = hoursController.getActiveHours();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(hoursService, times(1)).findActiveHours();
    }
    @Test
    @DisplayName("Should return 404 when no active hours exists")
    void shouldReturn404WhenNoActiveHoursExists() {
        when(hoursService.findActiveHours()).thenReturn(Optional.empty());

        ResponseEntity<ActiveHoursResponseDto> response = hoursController.getActiveHours();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(hoursService, times(1)).findActiveHours();
    }

    @Test
    @DisplayName("Should return 200 when get my hours")
    void shouldReturn200WhenGetMyHours() {

        List<HoursReportDto> list = List.of(
                new HoursReportDto(
                        1L,
                        "2026-05-17T10:00:00",
                        "2026-05-17T11:00:00",
                        3600,
                        "Atividade 1",
                        "APPROVED"
                ),
                new HoursReportDto(
                        2L,
                        "2026-05-17T12:00:00",
                        "2026-05-17T14:00:00",
                        7200,
                        "Atividade 2",
                        "PENDING"
                )
        );

        when(hoursReportService.getMyHours(2)).thenReturn(list);

        ResponseEntity<List<HoursReportDto>> response =
                hoursController.getMyHours(2);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(hoursReportService, times(1))
                .getMyHours(2);
    }

    @Test
    @DisplayName("Should return 200 with empty list when no hours registered")
    void shouldReturn200WithEmptyListWhenNoHoursRegistered() {

        when(hoursReportService.getMyHours(null))
                .thenReturn(List.of());

        ResponseEntity<List<HoursReportDto>> response =
                hoursController.getMyHours(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(hoursReportService, times(1))
                .getMyHours(null);
    }

    @Test
    @DisplayName("Should return 200 when get hour control")
    void shouldReturn200WhenGetHourControl() {
        HoursDTO dto = HoursDTO.builder()
                .completedSeconds(108000L)
                .remainingSeconds(108000L)
                .totalSeconds(216000L)
                .percentual(50.0)
                .build();
        when(hoursService.getHourControl()).thenReturn(dto);

        ResponseEntity<HoursDTO> response = hoursController.getHourControl();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(hoursService, times(1)).getHourControl();
    }

    @Test
    @DisplayName("Should return 201 when start hours successfully")
    void shouldReturn201WhenStartHoursSuccessfully() {
        StartHoursResponseDto dto = new StartHoursResponseDto(1, Instant.now());
        when(hoursService.startHours()).thenReturn(dto);

        ResponseEntity<StartHoursResponseDto> response = hoursController.startHours();

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(hoursService, times(1)).startHours();
    }

    @Test
    @DisplayName("Should throw exception when already has active session")
    void shouldThrowExceptionWhenAlreadyHasActiveSession() {
        when(hoursService.startHours()).thenThrow(new HoursAlreadyOpenException("Já existe sessão ativa"));

        assertThrows(HoursAlreadyOpenException.class, () -> hoursController.startHours());
        verify(hoursService, times(1)).startHours();
    }

    @Test
    @DisplayName("Should return 200 when stop hours successfully")
    void shouldReturn200WhenStopHoursSuccessfully() {
        StopHoursRequestDto request = new StopHoursRequestDto("Desenvolvi a feature X");
        StopHoursResponseDto dto = new StopHoursResponseDto(1, "Desenvolvi a feature X", Instant.now(), Instant.now(), 3600);
        when(hoursService.stopHours(request)).thenReturn(dto);

        ResponseEntity<StopHoursResponseDto> response = hoursController.stopHours(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(hoursService, times(1)).stopHours(request);
    }

    @Test
    @DisplayName("Should throw exception when no active session to stop")
    void shouldThrowExceptionWhenNoActiveSessionToStop() {
        StopHoursRequestDto request = new StopHoursRequestDto("Descrição");
        when(hoursService.stopHours(request)).thenThrow(new ActiveHoursNotFoundException("Nenhuma sessão ativa"));

        assertThrows(ActiveHoursNotFoundException.class, () -> hoursController.stopHours(request));
        verify(hoursService, times(1)).stopHours(request);
    }
}