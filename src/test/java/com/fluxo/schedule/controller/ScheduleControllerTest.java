package com.fluxo.schedule.controller;

import com.fluxo.infra.exception.GlobalExceptionHandler;
import com.fluxo.schedule.entity.Schedule;
import com.fluxo.schedule.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ScheduleController scheduleController = new ScheduleController(scheduleService);

        mockMvc = MockMvcBuilders.standaloneSetup(scheduleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listScheduleShouldReturnBadRequestWhenDiaTurnoIsMissing() throws Exception {
        mockMvc.perform(get("/schedule"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parametro diaTurno e obrigatorio e deve ser valido"));

        verifyNoInteractions(scheduleService);
    }

    @Test
    void listScheduleShouldReturnBadRequestWhenDiaTurnoIsInvalid() throws Exception {
        mockMvc.perform(get("/schedule").param("diaTurno", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parametro diaTurno e obrigatorio e deve ser valido"));

        verify(scheduleService, never()).listScheduleByPeriod("INVALIDO");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "JK_SEGQUA",
            "LM_SEGQUA",
            "JK_TERQUI",
            "LMNP_SEXTA"
    })
    void listScheduleShouldAcceptAllFrontendDiaTurnoValues(String diaTurno) throws Exception {
        when(scheduleService.listScheduleByPeriod(diaTurno)).thenReturn(List.of());

        mockMvc.perform(get("/schedule").param("diaTurno", diaTurno))
                .andExpect(status().isOk());

        verify(scheduleService).listScheduleByPeriod(diaTurno);
    }

    @Test
    void listScheduleShouldReturnFrontendContractWhenDiaTurnoIsValid() throws Exception {
        Schedule schedule = new Schedule();
        schedule.setId(1);
        schedule.setEvent("Apresentacao da Sprint 3 para stakeholders e planning da Sprint 4");
        schedule.setEventDate(LocalDate.of(2025, 6, 5));
        schedule.setEventTime(LocalTime.of(19, 0));
        schedule.setEventPeriod("LM_SEGQUA");

        when(scheduleService.listScheduleByPeriod("LM_SEGQUA")).thenReturn(List.of(schedule));

        mockMvc.perform(get("/schedule").param("diaTurno", "LM_SEGQUA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].event").value("Apresentacao da Sprint 3 para stakeholders e planning da Sprint 4"))
                .andExpect(jsonPath("$[0].eventDate").value("2025-06-05"))
                .andExpect(jsonPath("$[0].eventTime").value("19:00"))
                .andExpect(jsonPath("$[0].eventPeriod").value("LM_SEGQUA"));

        verify(scheduleService).listScheduleByPeriod("LM_SEGQUA");
    }
}
