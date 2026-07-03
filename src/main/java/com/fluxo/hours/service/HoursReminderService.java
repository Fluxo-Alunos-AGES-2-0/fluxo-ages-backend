package com.fluxo.hours.service;

import com.fluxo.auth.service.EmailService;
import com.fluxo.hours.dto.OpenHoursReminderDispatchResponseDto;
import com.fluxo.hours.entity.HoursReport;
import com.fluxo.hours.repository.HoursReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HoursReminderService {

    private final HoursReportRepository hoursReportRepository;
    private final EmailService emailService;

    public OpenHoursReminderDispatchResponseDto dispatchOpenHoursReminderEmails(
            Duration reminderAge,
            Duration reminderWindow
    ) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime windowStart = now.minus(reminderAge).minus(reminderWindow);
        OffsetDateTime windowEnd = now.minus(reminderAge);

        List<HoursReport> matchingReports = hoursReportRepository
                .findByExitTimeIsNullAndEntryTimeBetween(windowStart, windowEnd);

        int sentEmails = 0;

        for (HoursReport report : matchingReports) {
            try {
                emailService.sendOpenHoursReminderEmail(
                        report.getStudentUser().getEmail(),
                        report.getStudentUser().getName(),
                        report.getEntryTime().toInstant(),
                        now.toInstant()
                );
                sentEmails++;
            } catch (Exception e) {
                System.err.println("Erro ao enviar lembrete de horas abertas para "
                        + report.getStudentUser().getEmail() + ": " + e.getMessage());
            }
        }

        return new OpenHoursReminderDispatchResponseDto(matchingReports.size(), sentEmails);
    }
}
