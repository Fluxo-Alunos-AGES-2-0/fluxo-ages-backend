package com.fluxo.hours.service;

import com.fluxo.auth.service.EmailService;
import com.fluxo.hours.dto.HoursDTO;
import com.fluxo.hours.dto.StartHoursResponseDto;
import com.fluxo.hours.dto.StopHoursRequestDto;
import com.fluxo.hours.dto.StopHoursResponseDto;
import com.fluxo.hours.entity.HoursReport;
import com.fluxo.hours.entity.HoursReportStatus;
import com.fluxo.hours.exception.ActiveHoursNotFoundException;
import com.fluxo.hours.exception.HoursAlreadyOpenException;
import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.project.entity.Project;
import com.fluxo.project.entity.Team;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.service.AuthenticatedUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoursServiceTest {

    @Mock
    private HoursReportRepository hoursReportRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private EmailService emailService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<StudentProfile> studentProfileQuery;

    private HoursService hoursService;

    @BeforeEach
    void setUp() {
        hoursService = new HoursService(
                hoursReportRepository,
                authenticatedUserService,
                emailService
        );

        ReflectionTestUtils.setField(hoursService, "entityManager", entityManager);
    }

    @Test
    void startHoursShouldThrowHoursAlreadyOpenExceptionWhenUserAlreadyHasActiveSession() {
        User user = createUser();

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(hoursReportRepository.existsByStudentUserIdAndExitTimeIsNull(user.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> hoursService.startHours())
                .isInstanceOf(HoursAlreadyOpenException.class);

        verify(hoursReportRepository, never()).save(any(HoursReport.class));
        verifyNoInteractions(emailService);
        verifyNoInteractions(entityManager);
    }

    @Test
    void startHoursShouldSaveApprovedReportAndReturnStartHoursResponseDto() {
        User user = createUser();
        Project project = createProject(10);
        OffsetDateTime now = OffsetDateTime.parse("2026-06-12T15:00:00Z");

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(hoursReportRepository.existsByStudentUserIdAndExitTimeIsNull(user.getId()))
                .thenReturn(false);

        mockCurrentProject(project);

        when(hoursReportRepository.save(any(HoursReport.class)))
                .thenAnswer(invocation -> {
                    HoursReport report = invocation.getArgument(0);
                    report.setId(100);
                    return report;
                });

        try (MockedStatic<OffsetDateTime> mockedOffsetDateTime =
                     mockStatic(OffsetDateTime.class, CALLS_REAL_METHODS)) {

            mockedOffsetDateTime
                    .when(() -> OffsetDateTime.now(ZoneOffset.UTC))
                    .thenReturn(now);

            StartHoursResponseDto response = hoursService.startHours();

            assertThat(response.id()).isEqualTo(100);
            assertThat(response.startTime()).isEqualTo(now.toInstant());
        }

        ArgumentCaptor<HoursReport> captor = ArgumentCaptor.forClass(HoursReport.class);
        verify(hoursReportRepository).save(captor.capture());

        HoursReport savedReport = captor.getValue();

        assertThat(savedReport.getStudentUser()).isEqualTo(user);
        assertThat(savedReport.getProject()).isEqualTo(project);
        assertThat(savedReport.getStatus()).isEqualTo(HoursReportStatus.APPROVED);
        assertThat(savedReport.getEntryTime()).isEqualTo(now);

        verify(emailService).sendHoursStartedEmail(
                user.getEmail(),
                user.getName(),
                now.toInstant()
        );
    }

    @Test
    void startHoursShouldContinueNormallyWhenStartEmailFails() {
        User user = createUser();
        Project project = createProject(10);
        OffsetDateTime now = OffsetDateTime.parse("2026-06-12T15:00:00Z");

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(hoursReportRepository.existsByStudentUserIdAndExitTimeIsNull(user.getId()))
                .thenReturn(false);

        mockCurrentProject(project);

        when(hoursReportRepository.save(any(HoursReport.class)))
                .thenAnswer(invocation -> {
                    HoursReport report = invocation.getArgument(0);
                    report.setId(101);
                    return report;
                });

        doThrow(new RuntimeException("Erro ao enviar email"))
                .when(emailService)
                .sendHoursStartedEmail(anyString(), anyString(), any(Instant.class));

        try (MockedStatic<OffsetDateTime> mockedOffsetDateTime =
                     mockStatic(OffsetDateTime.class, CALLS_REAL_METHODS)) {

            mockedOffsetDateTime
                    .when(() -> OffsetDateTime.now(ZoneOffset.UTC))
                    .thenReturn(now);

            StartHoursResponseDto response = hoursService.startHours();

            assertThat(response.id()).isEqualTo(101);
            assertThat(response.startTime()).isEqualTo(now.toInstant());
        }

        verify(hoursReportRepository).save(any(HoursReport.class));
        verify(emailService).sendHoursStartedEmail(
                user.getEmail(),
                user.getName(),
                now.toInstant()
        );
    }

    @Test
    void stopHoursShouldThrowActiveHoursNotFoundExceptionWhenThereIsNoActiveSession() {
        User user = createUser();

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(hoursReportRepository.findFirstByStudentUserIdAndExitTimeIsNullOrderByEntryTimeDesc(user.getId()))
                .thenReturn(Optional.empty());

        StopHoursRequestDto request = new StopHoursRequestDto(
                "Descrição válida para encerramento das horas."
        );

        assertThatThrownBy(() -> hoursService.stopHours(request))
                .isInstanceOf(ActiveHoursNotFoundException.class);

        verify(hoursReportRepository, never()).save(any(HoursReport.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void stopHoursShouldSaveDescriptionExitTimeAndTotalSecondsAndReturnStopHoursResponseDto() {
        User user = createUser();
        OffsetDateTime entryTime = OffsetDateTime.parse("2026-06-12T15:00:00Z");
        OffsetDateTime endTime = entryTime.plusMinutes(5);
        int expectedTotalSeconds = (int) Duration.between(entryTime, endTime).getSeconds();

        HoursReport openReport = createOpenHoursReport(200, entryTime);

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(hoursReportRepository.findFirstByStudentUserIdAndExitTimeIsNullOrderByEntryTimeDesc(user.getId()))
                .thenReturn(Optional.of(openReport));
        when(hoursReportRepository.save(any(HoursReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StopHoursRequestDto request = new StopHoursRequestDto(
                "Descrição válida para encerramento das horas."
        );

        StopHoursResponseDto response;

        try (MockedStatic<OffsetDateTime> mockedOffsetDateTime =
                     mockStatic(OffsetDateTime.class, CALLS_REAL_METHODS)) {

            mockedOffsetDateTime
                    .when(() -> OffsetDateTime.now(ZoneOffset.UTC))
                    .thenReturn(endTime);

            response = hoursService.stopHours(request);
        }

        assertThat(openReport.getActivities()).isEqualTo(request.description());
        assertThat(openReport.getExitTime()).isEqualTo(endTime);
        assertThat(openReport.getTotalTimeSeconds()).isEqualTo(expectedTotalSeconds);

        assertThat(response.id()).isEqualTo(200);
        assertThat(response.description()).isEqualTo(request.description());
        assertThat(response.startTime()).isEqualTo(entryTime.toInstant());
        assertThat(response.endTime()).isEqualTo(endTime.toInstant());
        assertThat(response.totalTimeSeconds()).isEqualTo(expectedTotalSeconds);

        verify(emailService).sendHoursStoppedEmail(
                user.getEmail(),
                user.getName(),
                entryTime.toInstant(),
                endTime.toInstant(),
                expectedTotalSeconds
        );
    }

    @Test
    void stopHoursShouldContinueNormallyWhenStopEmailFails() {
        User user = createUser();
        OffsetDateTime entryTime = OffsetDateTime.parse("2026-06-12T15:00:00Z");
        OffsetDateTime endTime = entryTime.plusMinutes(10);

        HoursReport openReport = createOpenHoursReport(201, entryTime);

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(hoursReportRepository.findFirstByStudentUserIdAndExitTimeIsNullOrderByEntryTimeDesc(user.getId()))
                .thenReturn(Optional.of(openReport));
        when(hoursReportRepository.save(any(HoursReport.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        doThrow(new RuntimeException("Erro ao enviar email"))
                .when(emailService)
                .sendHoursStoppedEmail(
                        anyString(),
                        anyString(),
                        any(Instant.class),
                        any(Instant.class),
                        anyInt()
                );

        StopHoursRequestDto request = new StopHoursRequestDto(
                "Descrição válida para encerramento das horas."
        );

        StopHoursResponseDto response;

        try (MockedStatic<OffsetDateTime> mockedOffsetDateTime =
                     mockStatic(OffsetDateTime.class, CALLS_REAL_METHODS)) {

            mockedOffsetDateTime
                    .when(() -> OffsetDateTime.now(ZoneOffset.UTC))
                    .thenReturn(endTime);

            response = hoursService.stopHours(request);
        }

        assertThat(response.id()).isEqualTo(201);
        assertThat(response.description()).isEqualTo(request.description());
        assertThat(response.endTime()).isEqualTo(endTime.toInstant());

        verify(hoursReportRepository).save(openReport);
        verify(emailService).sendHoursStoppedEmail(
                user.getEmail(),
                user.getName(),
                entryTime.toInstant(),
                endTime.toInstant(),
                600
        );
    }

    @Test
    void getHourControlShouldReturnCompletedRemainingTotalAndPercentageCorrectly() {
        User user = createUser();
        Project project = createProject(10);

        mockCurrentProject(project);

        HoursReport firstReport = createFinishedHoursReport(300, 1000);
        HoursReport secondReport = createFinishedHoursReport(301, 500);
        HoursReport reportWithoutTotal = createFinishedHoursReport(302, null);

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(hoursReportRepository.findByStudentUserIdAndProjectIdAndStatusAndExitTimeIsNotNull(
                user.getId(),
                project.getId(),
                HoursReportStatus.APPROVED
        )).thenReturn(List.of(firstReport, secondReport, reportWithoutTotal));

        HoursDTO response = hoursService.getHourControl();

        assertThat(response.getCompletedSeconds()).isEqualTo(1500L);
        assertThat(response.getRemainingSeconds()).isEqualTo(214500L);
        assertThat(response.getTotalSeconds()).isEqualTo(216000L);
        assertThat(response.getPercentual()).isEqualTo((1500 * 100.0) / 216000L);
        assertThat(response.getOwingSeconds()).isZero();

        verify(hoursReportRepository).findByStudentUserIdAndProjectIdAndStatusAndExitTimeIsNotNull(
                user.getId(),
                project.getId(),
                HoursReportStatus.APPROVED
        );
    }

    private User createUser() {
        User user = new User();
        user.setId(1);
        user.setName("Paulo Trevisan");
        user.setEmail("paulo@email.com");
        return user;
    }

    private Project createProject(Integer id) {
        Project project = new Project();
        project.setId(id);
        return project;
    }

    private void mockCurrentProject(Project project) {
        StudentProfile studentProfile = new StudentProfile();

        Team team = new Team();
        team.setProject(project);

        studentProfile.setTeam(team);

        when(entityManager.createQuery(anyString(), eq(StudentProfile.class)))
                .thenReturn(studentProfileQuery);
        when(studentProfileQuery.setParameter(eq("userId"), any()))
                .thenReturn(studentProfileQuery);
        when(studentProfileQuery.setMaxResults(1))
                .thenReturn(studentProfileQuery);
        when(studentProfileQuery.getResultList())
                .thenReturn(List.of(studentProfile));
    }

    private HoursReport createOpenHoursReport(Integer id, OffsetDateTime entryTime) {
        HoursReport report = new HoursReport();
        report.setId(id);
        report.setEntryTime(entryTime);
        report.setExitTime(null);
        return report;
    }

    private HoursReport createFinishedHoursReport(Integer id, Integer totalTimeSeconds) {
        HoursReport report = new HoursReport();
        report.setId(id);
        report.setStatus(HoursReportStatus.APPROVED);
        report.setExitTime(OffsetDateTime.parse("2026-06-12T16:00:00Z"));
        report.setTotalTimeSeconds(totalTimeSeconds);
        return report;
    }
}
