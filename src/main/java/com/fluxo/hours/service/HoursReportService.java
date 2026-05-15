package com.fluxo.hours.service;

import com.fluxo.hours.dto.HoursReportDto;
import com.fluxo.hours.entity.HoursReport;
import com.fluxo.hours.entity.HoursReportStatus;
import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.user.entity.User;
import com.fluxo.user.service.StudentService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HoursReportService {

    private final HoursReportRepository repository;
    private final StudentService studentService;

    public HoursReportService(HoursReportRepository repository, StudentService studentService) {
        this.repository = repository;
        this.studentService = studentService;
    }

    public List<HoursReportDto> getMyHours(Integer idProject) {
        User user = getAuthenticatedUser();
        Integer projectId = getAuthenticatedProjectId();

        if (projectId == null) {
            return List.of();
        }

        return repository
                .findByStudentUserIdAndProjectIdAndExitTimeIsNotNullOrderByEntryTimeDesc(
                        user.getId(),
                        idProject != null ? idProject : projectId
                )
                .stream()
                .map(this::toHoursReportDto)
                .toList();
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (User) principal;
    }

    private Integer getAuthenticatedProjectId() {
        return studentService.getLoggedStudentProfile()
                .map(profile -> profile.currentProject().id())
                .orElse(null);
    }

    private HoursReportDto toHoursReportDto(HoursReport report) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        OffsetDateTime entry = report.getEntryTime();
        OffsetDateTime exit = report.getExitTime();

        return new HoursReportDto(
                report.getId().longValue(),
                entry != null ? entry.format(dateFormat) : null,
                entry != null ? entry.format(timeFormat) : null,
                exit != null ? exit.format(timeFormat) : null,
                formatSeconds(report.getTotalTimeSeconds()),
                report.getActivities(),
                resolveHoursReportStatus(report).name()
        );
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

    private String formatSeconds(Integer seconds) {
        if (seconds == null) {
            return null;
        }

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);
    }
}
