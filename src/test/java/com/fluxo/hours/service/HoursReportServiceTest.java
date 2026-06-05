package com.fluxo.hours.service;

import com.fluxo.hours.dto.HoursReportDto;
import com.fluxo.hours.entity.HoursReport;
import com.fluxo.hours.entity.HoursReportStatus;
import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.user.dto.StudentProfileResponseDto;
import com.fluxo.user.entity.User;
import com.fluxo.user.service.AuthenticatedUserService;
import com.fluxo.user.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoursReportServiceTest {

    @Mock
    private HoursReportRepository hoursReportRepository;

    @Mock
    private StudentService studentService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private HoursReportService hoursReportService;

    @Test
    void getMyHoursShouldReturnEmptyListWhenStudentHasNoCurrentProject() {
        User authenticatedUser = createUser();

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(studentService.getLoggedStudentProfile()).thenReturn(Optional.empty());

        List<HoursReportDto> result = hoursReportService.getMyHours(null);

        assertTrue(result.isEmpty());

        verify(authenticatedUserService).getAuthenticatedUser();
        verify(studentService).getLoggedStudentProfile();
        verify(hoursReportRepository, never())
                .findByStudentUserIdAndProjectIdAndExitTimeIsNotNullOrderByEntryTimeDesc(any(), any());
    }

    @Test
    void getMyHoursShouldUseCurrentProjectWhenProjectIdIsNotProvided() {
        User authenticatedUser = createUser();
        StudentProfileResponseDto profile = createStudentProfileResponseDto(10);

        HoursReport newestReport = createHoursReport(
                2,
                OffsetDateTime.parse("2026-05-29T12:00:00-03:00"),
                OffsetDateTime.parse("2026-05-29T14:00:00-03:00"),
                7200,
                "Atividade mais recente",
                HoursReportStatus.APPROVED
        );

        HoursReport oldestReport = createHoursReport(
                1,
                OffsetDateTime.parse("2026-05-28T10:00:00-03:00"),
                OffsetDateTime.parse("2026-05-28T11:00:00-03:00"),
                3600,
                "Atividade mais antiga",
                HoursReportStatus.PENDING
        );

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(studentService.getLoggedStudentProfile()).thenReturn(Optional.of(profile));
        when(hoursReportRepository.findByStudentUserIdAndProjectIdAndExitTimeIsNotNullOrderByEntryTimeDesc(
                authenticatedUser.getId(),
                10
        )).thenReturn(List.of(newestReport, oldestReport));

        List<HoursReportDto> result = hoursReportService.getMyHours(null);

        assertEquals(2, result.size());

        assertEquals(2L, result.get(0).id());
        assertEquals("Atividade mais recente", result.get(0).activities());
        assertEquals("APPROVED", result.get(0).status());

        assertEquals(1L, result.get(1).id());
        assertEquals("Atividade mais antiga", result.get(1).activities());
        assertEquals("PENDING", result.get(1).status());

        verify(hoursReportRepository)
                .findByStudentUserIdAndProjectIdAndExitTimeIsNotNullOrderByEntryTimeDesc(
                        authenticatedUser.getId(),
                        10
                );
    }

    @Test
    void getMyHoursShouldUseProvidedProjectIdWhenProjectIdIsProvided() {
        User authenticatedUser = createUser();
        StudentProfileResponseDto profile = createStudentProfileResponseDto(10);

        HoursReport report = createHoursReport(
                1,
                OffsetDateTime.parse("2026-05-29T10:00:00-03:00"),
                OffsetDateTime.parse("2026-05-29T11:00:00-03:00"),
                3600,
                "Atividade do projeto informado",
                HoursReportStatus.APPROVED
        );

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(studentService.getLoggedStudentProfile()).thenReturn(Optional.of(profile));
        when(hoursReportRepository.findByStudentUserIdAndProjectIdAndExitTimeIsNotNullOrderByEntryTimeDesc(
                authenticatedUser.getId(),
                20
        )).thenReturn(List.of(report));

        List<HoursReportDto> result = hoursReportService.getMyHours(20);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Atividade do projeto informado", result.get(0).activities());

        verify(hoursReportRepository)
                .findByStudentUserIdAndProjectIdAndExitTimeIsNotNullOrderByEntryTimeDesc(
                        authenticatedUser.getId(),
                        20
                );
    }

    private User createUser() {
        User user = new User();
        user.setId(1);
        user.setName("Aluno");
        user.setEmail("aluno@email.com");
        user.setEnrollmentNumber("123456");
        user.setPassword("senha");
        user.setRole("STUDENT");
        return user;
    }

    private StudentProfileResponseDto createStudentProfileResponseDto(Integer projectId) {
        return new StudentProfileResponseDto(
                1,
                "Aluno",
                "aluno@email.com",
                "https://avatar.com/aluno.png",
                1,
                new StudentProfileResponseDto.CurrentProjectDto(
                        projectId,
                        "Projeto Teste"
                ),
                new StudentProfileResponseDto.ProfessorDto(
                        2,
                        "Professor"
                ),
                new StudentProfileResponseDto.AttendanceDto(
                        10,
                        8,
                        2
                )
        );
    }

    private HoursReport createHoursReport(
            Integer id,
            OffsetDateTime entryTime,
            OffsetDateTime exitTime,
            Integer totalTimeSeconds,
            String activities,
            HoursReportStatus status
    ) {
        HoursReport report = new HoursReport();
        report.setId(id);
        report.setEntryTime(entryTime);
        report.setExitTime(exitTime);
        report.setTotalTimeSeconds(totalTimeSeconds);
        report.setActivities(activities);
        report.setStatus(status);
        return report;
    }
}