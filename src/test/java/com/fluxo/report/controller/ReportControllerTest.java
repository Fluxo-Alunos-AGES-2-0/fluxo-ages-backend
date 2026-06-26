package com.fluxo.report.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.dto.ReportArchiveResponseDto;
import com.fluxo.report.dto.ReportUploadUrlResponseDto;
import com.fluxo.report.dto.SprintReportRequestDto;
import com.fluxo.report.dto.SprintReportResponseDto;
import com.fluxo.report.dto.ReportFeedbackResponseDto;
import com.fluxo.report.exception.ReportTemplateNotFoundException;
import com.fluxo.report.service.ReportService;
import com.fluxo.report.service.ReportTemplateService;
import com.fluxo.report.service.SprintReportService;
import com.fluxo.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController Unit Tests")
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @Mock
    private ReportTemplateService reportTemplateService;

    @Mock
    private SprintReportService sprintReportService;

    @InjectMocks
    private ReportController reportController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController)
                .setControllerAdvice(new com.fluxo.infra.exception.GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }
    @Test
    @DisplayName("GET /report/me/final returns HTTP 200 with final reports")
    void testGetMyFinalReportsSuccess() throws Exception {
        FinalReportResponseDto report1 = new FinalReportResponseDto(
                1,
                OffsetDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC),
                "Project Alpha",
                new BigDecimal("8.50"),
                "http://example.com/file1.pdf",
                new ReportFeedbackResponseDto(
                        "Excellent work",
                        "http://example.com/correction1.pdf",
                        OffsetDateTime.of(2024, 1, 20, 10, 0, 0, 0, ZoneOffset.UTC),
                        "Joao Almeida Severo"
                )
        );
        FinalReportResponseDto report2 = new FinalReportResponseDto(
                2,
                OffsetDateTime.of(2024, 2, 15, 0, 0, 0, 0, ZoneOffset.UTC),
                "Project Beta",
                new BigDecimal("9.00"),
                "http://example.com/file2.pdf",
                new ReportFeedbackResponseDto(
                        "Outstanding performance",
                        "http://example.com/correction2.pdf",
                        OffsetDateTime.of(2024, 2, 20, 10, 0, 0, 0, ZoneOffset.UTC),
                        "Joao Almeida Severo"
                )
        );

        when(reportService.getFinalReports()).thenReturn(List.of(report1, report2));

        mockMvc.perform(get("/report/me/final").contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].project", equalTo("Project Alpha")))
                .andExpect(jsonPath("$[0].grade", equalTo(8.50)))
                .andExpect(jsonPath("$[0].feedback.comment", equalTo("Excellent work")))
                .andExpect(jsonPath("$[0].feedback.correctionUrl", equalTo("http://example.com/correction1.pdf")))
                .andExpect(jsonPath("$[0].feedback.revisionDate", equalTo("2024-01-20T10:00:00Z")))
                .andExpect(jsonPath("$[1].project", equalTo("Project Beta")))
                .andExpect(jsonPath("$[1].grade", equalTo(9.00)))
                .andExpect(jsonPath("$[1].feedback.comment", equalTo("Outstanding performance")))
                .andExpect(jsonPath("$[1].feedback.correctionUrl", equalTo("http://example.com/correction2.pdf")))
                .andExpect(jsonPath("$[1].feedback.revisionDate", equalTo("2024-02-20T10:00:00Z")))
                .andDo(print());

        verify(reportService, times(1)).getFinalReports();
    }

    @Test
    @DisplayName("GET /report/me/final returns empty list when no reports exist")
    void testGetMyFinalReportsEmpty() throws Exception {
        when(reportService.getFinalReports()).thenReturn(List.of());

        mockMvc.perform(get("/report/me/final").contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andDo(print());

        verify(reportService, times(1)).getFinalReports();
    }

    @Test
    @DisplayName("GET /report/template returns DOCX template for download")
    void testDownloadReportTemplateSuccess() throws Exception {
        byte[] templateBytes = "template content".getBytes(StandardCharsets.UTF_8);
        Resource templateResource = new ByteArrayResource(templateBytes);

        when(reportTemplateService.loadReportTemplate()).thenReturn(templateResource);

        mockMvc.perform(get("/report/template"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report-template.docx\""))
                .andExpect(content().bytes(templateBytes));

        verify(reportTemplateService, times(1)).loadReportTemplate();
    }

    @Test
    @DisplayName("GET /report/template returns HTTP 404 when template is missing")
    void testDownloadReportTemplateNotFound() throws Exception {
        when(reportTemplateService.loadReportTemplate())
                .thenThrow(new ReportTemplateNotFoundException("Template de relatório não encontrado."));

        mockMvc.perform(get("/report/template"))
                .andExpect(status().isNotFound())
                .andDo(print());

        verify(reportTemplateService, times(1)).loadReportTemplate();
    }

    @Test
    @DisplayName("GET /report/me/progress returns HTTP 200 with progress reports")
    void testGetMyProgressReportsSuccess() throws Exception {
        ProgressReportResponseDto report1 = new ProgressReportResponseDto(
                1,
                OffsetDateTime.of(2024, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC),
                "Project Alpha",
                new BigDecimal("7.50"),
                "http://example.com/prog1.pdf",
                new ReportFeedbackResponseDto(
                        "Good progress",
                        "http://example.com/prog-correction1.pdf",
                        OffsetDateTime.of(2024, 1, 12, 10, 0, 0, 0, ZoneOffset.UTC),
                        "Joao Almeida Severo"
                )
        );
        ProgressReportResponseDto report2 = new ProgressReportResponseDto(
                2,
                OffsetDateTime.of(2024, 2, 10, 0, 0, 0, 0, ZoneOffset.UTC),
                "Project Beta",
                new BigDecimal("8.00"),
                "http://example.com/prog2.pdf",
                new ReportFeedbackResponseDto(
                        "Very good progress",
                        "http://example.com/prog-correction2.pdf",
                        OffsetDateTime.of(2024, 2, 12, 10, 0, 0, 0, ZoneOffset.UTC),
                        "Joao Almeida Severo"
                )
        );

        when(reportService.getProgressReports()).thenReturn(List.of(report1, report2));

        mockMvc.perform(get("/report/me/progress").contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].project", equalTo("Project Alpha")))
                .andExpect(jsonPath("$[0].grade", equalTo(7.50)))
                .andExpect(jsonPath("$[0].feedback.comment", equalTo("Good progress")))
                .andExpect(jsonPath("$[0].feedback.correctionUrl", equalTo("http://example.com/prog-correction1.pdf")))
                .andExpect(jsonPath("$[0].feedback.revisionDate", equalTo("2024-01-12T10:00:00Z")))
                .andExpect(jsonPath("$[1].project", equalTo("Project Beta")))
                .andExpect(jsonPath("$[1].grade", equalTo(8.00)))
                .andExpect(jsonPath("$[1].feedback.comment", equalTo("Very good progress")))
                .andExpect(jsonPath("$[1].feedback.correctionUrl", equalTo("http://example.com/prog-correction2.pdf")))
                .andExpect(jsonPath("$[1].feedback.revisionDate", equalTo("2024-02-12T10:00:00Z")))
                .andDo(print());

        verify(reportService, times(1)).getProgressReports();
    }

    @Test
    @DisplayName("GET /report/me/progress returns empty list when no reports exist")
    void testGetMyProgressReportsEmpty() throws Exception {
        when(reportService.getProgressReports()).thenReturn(List.of());

        mockMvc.perform(get("/report/me/progress").contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andDo(print());

        verify(reportService, times(1)).getProgressReports();
    }

    @Test
    @DisplayName("POST /report/sprint returns HTTP 201 with valid data")
    void testCreateSprintReportSuccess() throws Exception {
        SprintReportRequestDto request = new SprintReportRequestDto(
                1,
                "Implement login feature",
                "Completed login UI",
                "Database connection issue",
                "Use connection pooling",
                "Fix database connection and test integration"
        );

        SprintReportResponseDto response = new SprintReportResponseDto(
                1,
                1,
                "Implement login feature",
                "Completed login UI",
                "Database connection issue",
                "Use connection pooling",
                "Fix database connection and test integration"
        );

        when(sprintReportService.createSprintReport(any(SprintReportRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", equalTo(1)))
                .andExpect(jsonPath("$.sprint", equalTo(1)))
                .andExpect(jsonPath("$.predictedActivity", equalTo("Implement login feature")))
                .andDo(print());

        verify(sprintReportService, times(1)).createSprintReport(any(SprintReportRequestDto.class));
    }

    @Test
    @DisplayName("POST /report/sprint persists correct data")
    void testCreateSprintReportDataPersistence() throws Exception {
        SprintReportRequestDto request = new SprintReportRequestDto(
                2,
                "Implement payment gateway",
                "Integrated Stripe API",
                "SSL certificate issues",
                "Always use HTTPS",
                "Deploy to production"
        );

        SprintReportResponseDto response = new SprintReportResponseDto(
                2, 2,
                "Implement payment gateway",
                "Integrated Stripe API",
                "SSL certificate issues",
                "Always use HTTPS",
                "Deploy to production"
        );

        when(sprintReportService.createSprintReport(any(SprintReportRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        verify(sprintReportService).createSprintReport(argThat(dto ->
                dto.sprint().equals(2) &&
                dto.predictedActivity().equals("Implement payment gateway")
        ));
    }

    @Test
    @DisplayName("POST /report/sprint returns HTTP 400 when sprint is null")
    void testCreateSprintReportMissingSprintField() throws Exception {
        String request = "{\"predictedActivity\":\"Activity\",\"activityCompleted\":\"Completed\",\"problemsEncountered\":\"Problems\",\"learnedLessons\":\"Lessons\",\"nextSteps\":\"Steps\"}";

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(sprintReportService, never()).createSprintReport(any());
    }

    @Test
    @DisplayName("POST /report/sprint returns HTTP 400 when sprint is below minimum")
    void testCreateSprintReportInvalidSprintBelowMin() throws Exception {
        String request = "{\"sprint\":-1,\"predictedActivity\":\"Activity\",\"activityCompleted\":\"Completed\",\"problemsEncountered\":\"Problems\",\"learnedLessons\":\"Lessons\",\"nextSteps\":\"Steps\"}";

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(sprintReportService, never()).createSprintReport(any());
    }

    @Test
    @DisplayName("POST /report/sprint returns HTTP 400 when sprint exceeds maximum")
    void testCreateSprintReportInvalidSprintAboveMax() throws Exception {
        String request = "{\"sprint\":6,\"predictedActivity\":\"Activity\",\"activityCompleted\":\"Completed\",\"problemsEncountered\":\"Problems\",\"learnedLessons\":\"Lessons\",\"nextSteps\":\"Steps\"}";

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(sprintReportService, never()).createSprintReport(any());
    }

    @Test
    @DisplayName("POST /report/sprint returns HTTP 400 when predictedActivity is blank")
    void testCreateSprintReportMissingPredictedActivity() throws Exception {
        String request = "{\"sprint\":1,\"predictedActivity\":\"\",\"activityCompleted\":\"Completed\",\"problemsEncountered\":\"Problems\",\"learnedLessons\":\"Lessons\",\"nextSteps\":\"Steps\"}";

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());

        verify(sprintReportService, never()).createSprintReport(any());
    }

    @Test
    @DisplayName("POST /report/sprint returns HTTP 400 when activityCompleted is blank")
    void testCreateSprintReportMissingActivityCompleted() throws Exception {
        String request = "{\"sprint\":1,\"predictedActivity\":\"Activity\",\"activityCompleted\":\"\",\"problemsEncountered\":\"Problems\",\"learnedLessons\":\"Lessons\",\"nextSteps\":\"Steps\"}";

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());

        verify(sprintReportService, never()).createSprintReport(any());
    }

    @Test
    @DisplayName("POST /report/sprint returns HTTP 400 when problemsEncountered is blank")
    void testCreateSprintReportMissingProblemsEncountered() throws Exception {
        String request = "{\"sprint\":1,\"predictedActivity\":\"Activity\",\"activityCompleted\":\"Completed\",\"problemsEncountered\":\"\",\"learnedLessons\":\"Lessons\",\"nextSteps\":\"Steps\"}";

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());

        verify(sprintReportService, never()).createSprintReport(any());
    }

    @Test
    @DisplayName("POST /report/sprint returns HTTP 400 when learnedLessons is blank")
    void testCreateSprintReportMissingLearnedLessons() throws Exception {
        String request = "{\"sprint\":1,\"predictedActivity\":\"Activity\",\"activityCompleted\":\"Completed\",\"problemsEncountered\":\"Problems\",\"learnedLessons\":\"\",\"nextSteps\":\"Steps\"}";

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());

        verify(sprintReportService, never()).createSprintReport(any());
    }

    @Test
    @DisplayName("POST /report/sprint returns HTTP 400 when nextSteps is blank")
    void testCreateSprintReportMissingNextSteps() throws Exception {
        String request = "{\"sprint\":1,\"predictedActivity\":\"Activity\",\"activityCompleted\":\"Completed\",\"problemsEncountered\":\"Problems\",\"learnedLessons\":\"Lessons\",\"nextSteps\":\"\"}";

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest());

        verify(sprintReportService, never()).createSprintReport(any());
    }

    @Test
    @DisplayName("POST /report/sprint returns HTTP 400 with invalid JSON")
    void testCreateSprintReportInvalidJson() throws Exception {
        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(sprintReportService, never()).createSprintReport(any());
    }

    @Test
    @DisplayName("POST /report/sprint accepts valid sprint values 1-5")
    void testCreateSprintReportValidSprintRange() throws Exception {
        for (int sprint = 1; sprint <= 5; sprint++) {
            SprintReportRequestDto request = new SprintReportRequestDto(
                    sprint,
                    "Activity",
                    "Completed",
                    "Problems",
                    "Lessons",
                    "Steps"
            );

            SprintReportResponseDto response = new SprintReportResponseDto(
                    sprint, sprint,
                    "Activity",
                    "Completed",
                    "Problems",
                    "Lessons",
                    "Steps"
            );

            when(sprintReportService.createSprintReport(any(SprintReportRequestDto.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/report/sprint")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            reset(sprintReportService);
        }
    }

    @Test
    @DisplayName("POST /report/sprint service receives all fields correctly")
    void testCreateSprintReportServiceReceivesAllFields() throws Exception {
        SprintReportRequestDto request = new SprintReportRequestDto(
                3,
                "Predicted Activity",
                "Activity Completed",
                "Problems Found",
                "Lessons Learned",
                "Next Steps"
        );

        SprintReportResponseDto response = new SprintReportResponseDto(
                3, 3,
                "Predicted Activity",
                "Activity Completed",
                "Problems Found",
                "Lessons Learned",
                "Next Steps"
        );

        when(sprintReportService.createSprintReport(any(SprintReportRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        verify(sprintReportService).createSprintReport(argThat(dto ->
                dto.sprint() == 3 &&
                dto.predictedActivity().equals("Predicted Activity") &&
                dto.activityCompleted().equals("Activity Completed") &&
                dto.problemsEncountered().equals("Problems Found") &&
                dto.learnedLessons().equals("Lessons Learned") &&
                dto.nextSteps().equals("Next Steps")
        ));
    }

    @Test
    @DisplayName("POST /report/sprint does not call service when validation fails")
    void testCreateSprintReportNoServiceCallOnValidationFailure() throws Exception {
        String request = "{\"sprint\":1}";

        mockMvc.perform(post("/report/sprint")
                .contentType(APPLICATION_JSON)
                .content(request));

        verify(sprintReportService, never()).createSprintReport(any());
    }
}
