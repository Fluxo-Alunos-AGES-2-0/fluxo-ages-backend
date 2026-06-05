package com.fluxo.hours.service;

import com.fluxo.hours.dto.HoursReportDto;
import com.fluxo.hours.dto.UpdateHoursReportRequestDto;
import com.fluxo.hours.dto.UpdateHoursReportResponseDto;
import com.fluxo.hours.entity.HoursReport;
import com.fluxo.hours.entity.HoursReportStatus;
import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.report.exception.ReportNotFoundException;
import com.fluxo.user.entity.User;
import com.fluxo.user.service.AuthenticatedUserService;
import com.fluxo.user.service.StudentService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class HoursReportService {

    private final HoursReportRepository hoursReportRepository;
    private final StudentService studentService;
    private final AuthenticatedUserService authenticatedUserService;

    public List<HoursReportDto> getMyHours(Integer idProject) {
        User user = authenticatedUserService.getAuthenticatedUser();
        Integer projectId = getAuthenticatedProjectId();

        if (projectId == null) {
            return List.of();
        }

        return hoursReportRepository
                .findByStudentUserIdAndProjectIdAndExitTimeIsNotNullOrderByEntryTimeDesc(
                        user.getId(),
                        idProject != null ? idProject : projectId)
                .stream()
                .map(this::toHoursReportDto)
                .toList();
    }

    public UpdateHoursReportResponseDto updateHoursReport(Integer id, UpdateHoursReportRequestDto request) {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        HoursReport report = getHoursReportById(id);

        if (!report.getStudentUser().getId().equals(authenticatedUser.getId())) {
            throw new AccessDeniedException("Você não tem permissão para editar este relatório.");
        }

        int totalTimeSeconds = (int) Duration
                .between(request.entryTime().toInstant(), request.exitTime().toInstant())
                .getSeconds();

        report.setEntryTime(request.entryTime());
        report.setExitTime(request.exitTime());
        report.setActivities(request.activities().trim());
        report.setEditDate(OffsetDateTime.now());
        report.setStatus(HoursReportStatus.PENDING);
        report.setTotalTimeSeconds(totalTimeSeconds);

        HoursReport updatedHoursReport = hoursReportRepository.save(report);

        return new UpdateHoursReportResponseDto(
                report.getId(),
                updatedHoursReport.getEntryTime(),
                updatedHoursReport.getExitTime(),
                report.getTotalTimeSeconds(),
                updatedHoursReport.getActivities(),
                report.getEditDate()
        );
    }

    public HoursReport getHoursReportById(Integer reportId) {
        HoursReport hoursReport = hoursReportRepository.findHoursReportById(reportId);
        if (hoursReport == null)
            throw new ReportNotFoundException("Relatório de id '" + reportId + "' não foi encontrado");
        return hoursReport;
    }

    private Integer getAuthenticatedProjectId() {
        return studentService.getLoggedStudentProfile()
                .map(profile -> profile.currentProject().id())
                .orElse(null);
    }

    private HoursReportDto toHoursReportDto(HoursReport report) {
        OffsetDateTime entry = report.getEntryTime();
        OffsetDateTime exit = report.getExitTime();

        return new HoursReportDto(
                report.getId().longValue(),
                entry != null
                        ? entry.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        : null,
                exit != null
                        ? exit.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        : null,
                report.getTotalTimeSeconds(),
                report.getActivities(),
                resolveHoursReportStatus(report).name(),
                report.getRejectionJustification());
    }

    private HoursReportStatus resolveHoursReportStatus(HoursReport report) {
        if (report.getStatus() != null) {
            return report.getStatus();
        }
        if (report.getRejectionJustification() != null && !report.getRejectionJustification().isBlank()) {
            return HoursReportStatus.REJECTED;
        }
        if (report.getExitTime() == null) {
            return HoursReportStatus.PENDING;
        }
        return HoursReportStatus.APPROVED;
    }

}
