package com.fluxo.report.controller;

import com.fluxo.report.service.ReportService;
import com.fluxo.report.service.ReportTemplateService;
import com.fluxo.report.service.SprintReportService;
import com.fluxo.infra.config.JwtAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc
@DisplayName("Report template security tests")
class ReportTemplateSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private ReportTemplateService reportTemplateService;

    @MockBean
    private SprintReportService sprintReportService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /report/template without token returns 401")
    void testDownloadReportTemplateWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/report/template"))
                .andExpect(status().isUnauthorized());
    }
}
