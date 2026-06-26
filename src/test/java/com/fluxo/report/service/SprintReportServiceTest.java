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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SprintReportService Unit Tests")
class SprintReportServiceTest {

    @Mock
    private SprintReportRepository sprintReportRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private SprintReportService sprintReportService;

    @Captor
    private ArgumentCaptor<SprintReport> sprintReportCaptor;

    private User authenticatedUser;
    private Project project;
    private StudentProfile studentProfile;

    @BeforeEach
    void setUp() {
        authenticatedUser = new User();
        authenticatedUser.setId(999);
        authenticatedUser.setName("Sprint Student");

        project = new Project();
        project.setId(55);
        project.setName("Sprint Project");

        Team team = new Team();
        team.setProject(project);

        studentProfile = new StudentProfile();
        studentProfile.setStudentUser(authenticatedUser);
        studentProfile.setTeam(team);
    }

    @Test
    @DisplayName("createSprintReport creates a new sprint report when none exists")
    void createSprintReport_createsNewReportWhenNoneExists() {
        SprintReportRequestDto request = new SprintReportRequestDto(
                2,
                "Predicted activity",
                "Completed activity",
                "Problems encountered",
                "Learned lessons",
                "Next steps"
        );

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(studentProfileRepository.findByStudentUserId(authenticatedUser.getId()))
                .thenReturn(Optional.of(studentProfile));
        when(sprintReportRepository.findByStudentUserIdAndProjectIdAndSprintAndType(
                authenticatedUser.getId(), project.getId(), "2", ReportType.SPRINT
        )).thenReturn(Optional.empty());
        when(sprintReportRepository.save(any(SprintReport.class))).thenAnswer(invocation -> {
            SprintReport saved = invocation.getArgument(0);
            saved.setId(22);
            return saved;
        });

        SprintReportResponseDto response = sprintReportService.createSprintReport(request);

        verify(sprintReportRepository, times(1)).save(sprintReportCaptor.capture());
        SprintReport savedReport = sprintReportCaptor.getValue();

        assertEquals(22, response.id());
        assertEquals(2, response.sprint());
        assertEquals("Predicted activity", response.predictedActivity());
        assertEquals("Completed activity", response.activityCompleted());

        assertEquals(authenticatedUser, savedReport.getStudentUser());
        assertEquals(project, savedReport.getProject());
        assertEquals("2", savedReport.getSprint());
        assertEquals(ReportType.SPRINT, savedReport.getType());
        assertNotNull(savedReport.getCreateDate());
        assertNotNull(savedReport.getEditDate());
    }

    @Test
    @DisplayName("createSprintReport throws when student profile is missing")
    void createSprintReport_throwsWhenStudentProfileMissing() {
        SprintReportRequestDto request = new SprintReportRequestDto(
                1,
                "Predicted activity",
                "Completed activity",
                "Problems encountered",
                "Learned lessons",
                "Next steps"
        );

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(studentProfileRepository.findByStudentUserId(authenticatedUser.getId()))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> sprintReportService.createSprintReport(request));

        assertEquals("Perfil do aluno autenticado não encontrado.", exception.getMessage());
    }

    @Test
    @DisplayName("createSprintReport throws when project is missing")
    void createSprintReport_throwsWhenProjectMissing() {
        SprintReportRequestDto request = new SprintReportRequestDto(
                1,
                "Predicted activity",
                "Completed activity",
                "Problems encountered",
                "Learned lessons",
                "Next steps"
        );

        studentProfile.setTeam(null);
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(studentProfileRepository.findByStudentUserId(authenticatedUser.getId()))
                .thenReturn(Optional.of(studentProfile));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> sprintReportService.createSprintReport(request));

        assertEquals("Projeto do aluno autenticado não encontrado.", exception.getMessage());
    }
}
