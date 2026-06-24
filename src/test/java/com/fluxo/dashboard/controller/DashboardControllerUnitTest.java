package com.fluxo.dashboard.controller;

import com.fluxo.auth.service.JwtService;
import com.fluxo.dashboard.dto.DashboardResponseDTO;
import com.fluxo.dashboard.dto.ProfileDTO;
import com.fluxo.dashboard.service.DashboardService;
import com.fluxo.hours.dto.HoursDTO;
import com.fluxo.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@DisplayName("DashboardController unit tests")
class DashboardControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("GET /dashboard - returns 200 with dashboard data")
    void getDashboard_validUser_returns200WithBody() throws Exception {
        Integer userId = 1;

        HoursDTO hours = HoursDTO.builder()
                .completedSeconds(3600)
                .remainingSeconds(7200)
                .totalSeconds(10800)
                .percentual(33.3)
                .owingSeconds(0)
                .build();

        ProfileDTO profile = ProfileDTO.builder()
                .id(userId)
                .name("Ciclano De tal")
                .email("talCiclano@example.com")
                .avatarUrl("https://example.com/avatar.png")
                .agesLevel(2)
                .build();

        DashboardResponseDTO response = new DashboardResponseDTO(profile, hours);

        when(dashboardService.getDashboard(any())).thenReturn(response);

        mockMvc.perform(get("/dashboard")
                        .with(user("talCiclano@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profile.id").value(userId))
                .andExpect(jsonPath("$.profile.name").value("Ciclano De tal"))
                .andExpect(jsonPath("$.profile.email").value("talCiclano@example.com"))
                .andExpect(jsonPath("$.hours.completedSeconds").value(3600))
                .andExpect(jsonPath("$.hours.remainingSeconds").value(7200))
                .andExpect(jsonPath("$.hours.totalSeconds").value(10800))
                .andExpect(jsonPath("$.hours.percentual").value(33.3))
                .andExpect(jsonPath("$.hours.owingSeconds").value(0));

        verify(dashboardService, times(1)).getDashboard(any());
    }

    @Test
    @DisplayName("GET /dashboard - service is called exactly once")
    void getDashboard_validUser_callsServiceOnce() throws Exception {
        DashboardResponseDTO response = new DashboardResponseDTO(
                ProfileDTO.builder().id(2).name("Fulana").email("fulana@example.com").build(),
                HoursDTO.builder().completedSeconds(0).remainingSeconds(0).totalSeconds(0).build()
        );
        when(dashboardService.getDashboard(any())).thenReturn(response);

        mockMvc.perform(get("/dashboard")
                        .with(user("talCiclano@example.com").roles("USER")))
                .andExpect(status().isOk());

        verify(dashboardService, times(1)).getDashboard(any());
    }

    @Test
    @DisplayName("GET /dashboard - response Content-Type is application/json")
    void getDashboard_validUser_returnsJsonContentType() throws Exception {
        DashboardResponseDTO response = new DashboardResponseDTO(
                ProfileDTO.builder().id(1).name("Ciclano De tal").email("talCiclano@example.com").build(),
                HoursDTO.builder().completedSeconds(0).remainingSeconds(0).totalSeconds(0).build()
        );

        when(dashboardService.getDashboard(any())).thenReturn(response);

        mockMvc.perform(get("/dashboard")
                        .with(user("talCiclano@example.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }
}