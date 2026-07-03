package com.fluxo.hours.service;

import com.fluxo.auth.service.EmailService;
import com.fluxo.hours.dto.OpenHoursReminderDispatchResponseDto;
import com.fluxo.hours.entity.HoursReport;
import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HoursReminderServiceTest {

    @Mock
    private HoursReportRepository hoursReportRepository;

    @Mock
    private EmailService emailService;

    @Test
    void dispatchOpenHoursReminderEmailsShouldUseReminderWindowAndContinueOnFailure() {
        HoursReminderService service = new HoursReminderService(hoursReportRepository, emailService);
        OffsetDateTime now = OffsetDateTime.parse("2026-07-02T16:00:00Z");
        User firstUser = createUser("paulo@email.com", "Paulo Trevisan");
        User secondUser = createUser("segundo@email.com", "Segundo Aluno");

        HoursReport firstReport = createOpenReport(firstUser, OffsetDateTime.parse("2026-07-02T13:50:00Z"));
        HoursReport secondReport = createOpenReport(secondUser, OffsetDateTime.parse("2026-07-02T13:55:00Z"));

        when(hoursReportRepository.findByExitTimeIsNullAndEntryTimeBetween(any(), any()))
                .thenReturn(List.of(firstReport, secondReport));

        doAnswer(invocation -> {
            String to = invocation.getArgument(0, String.class);
            if (secondUser.getEmail().equals(to)) {
                throw new RuntimeException("smtp error");
            }
            return null;
        }).when(emailService).sendOpenHoursReminderEmail(any(), any(), any(), any());

        OpenHoursReminderDispatchResponseDto response;

        try (MockedStatic<OffsetDateTime> mockedOffsetDateTime =
                     mockStatic(OffsetDateTime.class, CALLS_REAL_METHODS)) {
            mockedOffsetDateTime
                    .when(() -> OffsetDateTime.now(ZoneOffset.UTC))
                    .thenReturn(now);

            response = service.dispatchOpenHoursReminderEmails(Duration.ofHours(2), Duration.ofMinutes(15));
        }

        ArgumentCaptor<OffsetDateTime> startCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> endCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        verify(hoursReportRepository).findByExitTimeIsNullAndEntryTimeBetween(startCaptor.capture(), endCaptor.capture());

        assertThat(startCaptor.getValue()).isEqualTo(now.minusHours(2).minusMinutes(15));
        assertThat(endCaptor.getValue()).isEqualTo(now.minusHours(2));
        assertThat(response.matchedSessions()).isEqualTo(2);
        assertThat(response.sentEmails()).isEqualTo(1);
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setId(1);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private HoursReport createOpenReport(User user, OffsetDateTime entryTime) {
        HoursReport report = new HoursReport();
        report.setStudentUser(user);
        report.setEntryTime(entryTime);
        report.setExitTime(null);
        return report;
    }
}
