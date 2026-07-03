package com.fluxo.report.service;

import com.fluxo.project.entity.Project;
import com.fluxo.project.entity.Team;
import com.fluxo.report.dto.SprintReportListResponseDto;
import com.fluxo.report.dto.SprintReportRequestDto;
import com.fluxo.report.dto.SprintReportResponseDto;
import com.fluxo.report.entity.SprintReport;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.repository.SprintReportRepository;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SprintReportServiceTest {

    @Mock
    private SprintReportRepository sprintReportRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private SprintReportService sprintReportService;

    @Test
    @DisplayName("createSprintReport creates a new sprint report when the sprint does not exist")
    void createSprintReportCreatesNewReportWhenSprintDoesNotExist() {
        User authenticatedUser = createUser(7, "Aluno");
        Project currentProject = createProject(11, "Projeto Atual");
        StudentProfile studentProfile = createStudentProfile(authenticatedUser, currentProject);
        SprintReportRequestDto request = new SprintReportRequestDto(
                2,
                "Atividade prevista",
                "Atividade concluida",
                "Problemas",
                "Licoes",
                "Proximos passos"
        );

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(studentProfileRepository.findByStudentUserId(7)).thenReturn(Optional.of(studentProfile));
        when(sprintReportRepository.findByStudentUserIdAndProjectIdAndSprintAndType(7, 11, "2", ReportType.SPRINT))
                .thenReturn(Optional.empty());
        when(sprintReportRepository.save(any(SprintReport.class)))
                .thenAnswer(invocation -> {
                    SprintReport report = invocation.getArgument(0);
                    report.setId(51);
                    return report;
                });

        SprintReportResponseDto response = sprintReportService.createSprintReport(request);

        ArgumentCaptor<SprintReport> reportCaptor = ArgumentCaptor.forClass(SprintReport.class);
        verify(sprintReportRepository).save(reportCaptor.capture());

        SprintReport savedReport = reportCaptor.getValue();
        assertEquals(51, response.id());
        assertEquals(2, response.sprint());
        assertEquals("Atividade prevista", response.predictedActivity());
        assertEquals(ReportType.SPRINT, savedReport.getType());
        assertEquals("2", savedReport.getSprint());
        assertEquals(authenticatedUser, savedReport.getStudentUser());
        assertEquals(currentProject, savedReport.getProject());
        assertNotNull(savedReport.getCreateDate());
        assertNotNull(savedReport.getEditDate());
    }

    @Test
    @DisplayName("createSprintReport updates the existing sprint report for the same sprint")
    void createSprintReportUpdatesExistingSprintReport() {
        User authenticatedUser = createUser(7, "Aluno");
        Project currentProject = createProject(11, "Projeto Atual");
        StudentProfile studentProfile = createStudentProfile(authenticatedUser, currentProject);
        SprintReportRequestDto request = new SprintReportRequestDto(
                3,
                "Nova atividade prevista",
                "Nova atividade concluida",
                "Novos problemas",
                "Novas licoes",
                "Novos proximos passos"
        );

        SprintReport existingReport = createSprintReport(
                61,
                "3",
                authenticatedUser,
                currentProject,
                OffsetDateTime.parse("2026-06-01T10:15:30Z")
        );

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(studentProfileRepository.findByStudentUserId(7)).thenReturn(Optional.of(studentProfile));
        when(sprintReportRepository.findByStudentUserIdAndProjectIdAndSprintAndType(7, 11, "3", ReportType.SPRINT))
                .thenReturn(Optional.of(existingReport));
        when(sprintReportRepository.save(existingReport)).thenReturn(existingReport);

        SprintReportResponseDto response = sprintReportService.createSprintReport(request);

        assertEquals(61, response.id());
        assertEquals(3, response.sprint());
        assertEquals("Nova atividade prevista", existingReport.getPredictedActivity());
        assertEquals("Nova atividade concluida", existingReport.getActivityCompleted());
        assertEquals("Novos problemas", existingReport.getProblemsEncountered());
        assertEquals("Novas licoes", existingReport.getLearnedLessons());
        assertEquals("Novos proximos passos", existingReport.getNextSteps());
        assertNotNull(existingReport.getEditDate());
    }

    @Test
    @DisplayName("createSprintReport throws when the authenticated student has no profile")
    void createSprintReportThrowsWhenStudentHasNoProfile() {
        User authenticatedUser = createUser(7, "Aluno");
        SprintReportRequestDto request = new SprintReportRequestDto(
                1,
                "Atividade prevista",
                "Atividade concluida",
                "Problemas",
                "Licoes",
                "Proximos passos"
        );

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(studentProfileRepository.findByStudentUserId(7)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sprintReportService.createSprintReport(request)
        );

        assertTrue(exception.getMessage().contains("Perfil do aluno autenticado"));
        verify(sprintReportRepository, never()).save(any(SprintReport.class));
    }

    @Test
    @DisplayName("createSprintReport throws when the authenticated student has no associated project")
    void createSprintReportThrowsWhenStudentHasNoAssociatedProject() {
        User authenticatedUser = createUser(7, "Aluno");
        SprintReportRequestDto request = new SprintReportRequestDto(
                1,
                "Atividade prevista",
                "Atividade concluida",
                "Problemas",
                "Licoes",
                "Proximos passos"
        );

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setStudentUser(authenticatedUser);
        studentProfile.setTeam(null);

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(studentProfileRepository.findByStudentUserId(7)).thenReturn(Optional.of(studentProfile));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sprintReportService.createSprintReport(request)
        );

        assertTrue(exception.getMessage().contains("Projeto do aluno autenticado"));
        verify(sprintReportRepository, never()).save(any(SprintReport.class));
    }

    @Test
    @DisplayName("getMySprintReports returns reports ordered by sprint number")
    void getMySprintReportsReturnsReportsOrderedBySprintNumber() {
        User authenticatedUser = createUser(7, "Aluno");
        Project currentProject = createProject(11, "Projeto Atual");

        SprintReport sprintFour = createSprintReport(71, "4", authenticatedUser, currentProject, OffsetDateTime.parse("2026-06-04T10:15:30Z"));
        sprintFour.setGrade(BigDecimal.valueOf(8.0));
        SprintReport sprintOne = createSprintReport(72, "1", authenticatedUser, currentProject, OffsetDateTime.parse("2026-06-01T10:15:30Z"));
        SprintReport sprintTwo = createSprintReport(73, " 2 ", authenticatedUser, currentProject, OffsetDateTime.parse("2026-06-02T10:15:30Z"));

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(sprintReportRepository.findByStudentUserIdAndType(7, ReportType.SPRINT))
                .thenReturn(List.of(sprintFour, sprintOne, sprintTwo));

        List<SprintReportListResponseDto> response = sprintReportService.getMySprintReports(null);

        assertEquals(3, response.size());
        assertEquals("Sprint 1", response.get(0).sprint());
        assertEquals("Sprint 2", response.get(1).sprint());
        assertEquals("Sprint 4", response.get(2).sprint());
        assertEquals(BigDecimal.valueOf(8.0), response.get(2).grade());

        verify(sprintReportRepository).findByStudentUserIdAndType(7, ReportType.SPRINT);
        verify(sprintReportRepository, never()).findByStudentUserIdAndProjectIdAndType(7, 11, ReportType.SPRINT);
    }

    @Test
    @DisplayName("getMySprintReports filters by projectId when it is provided")
    void getMySprintReportsFiltersByProjectIdWhenProvided() {
        User authenticatedUser = createUser(7, "Aluno");
        Project filteredProject = createProject(99, "Projeto Filtrado");

        SprintReport sprintThree = createSprintReport(81, "3", authenticatedUser, filteredProject, OffsetDateTime.parse("2026-06-03T10:15:30Z"));
        SprintReport sprintOne = createSprintReport(82, "1", authenticatedUser, filteredProject, OffsetDateTime.parse("2026-06-01T10:15:30Z"));

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(sprintReportRepository.findByStudentUserIdAndProjectIdAndType(7, 99, ReportType.SPRINT))
                .thenReturn(List.of(sprintThree, sprintOne));

        List<SprintReportListResponseDto> response = sprintReportService.getMySprintReports(99);

        assertEquals(2, response.size());
        assertEquals("Sprint 1", response.get(0).sprint());
        assertEquals("Sprint 3", response.get(1).sprint());
        assertEquals(99, response.get(0).projectId());

        verify(sprintReportRepository).findByStudentUserIdAndProjectIdAndType(7, 99, ReportType.SPRINT);
        verify(sprintReportRepository, never()).findByStudentUserIdAndType(7, ReportType.SPRINT);
    }

    private User createUser(Integer id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    private Project createProject(Integer id, String name) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        return project;
    }

    private StudentProfile createStudentProfile(User studentUser, Project project) {
        Team team = new Team();
        team.setProject(project);

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setStudentUser(studentUser);
        studentProfile.setTeam(team);
        return studentProfile;
    }

    private SprintReport createSprintReport(
            Integer id,
            String sprint,
            User studentUser,
            Project project,
            OffsetDateTime createDate
    ) {
        SprintReport report = new SprintReport();
        report.setId(id);
        report.setType(ReportType.SPRINT);
        report.setSprint(sprint);
        report.setStudentUser(studentUser);
        report.setProject(project);
        report.setCreateDate(createDate);
        report.setEditDate(createDate);
        report.setPredictedActivity("Prevista " + sprint.trim());
        report.setActivityCompleted("Concluida " + sprint.trim());
        report.setProblemsEncountered("Problemas " + sprint.trim());
        report.setLearnedLessons("Licoes " + sprint.trim());
        report.setNextSteps("Passos " + sprint.trim());
        return report;
    }
}
