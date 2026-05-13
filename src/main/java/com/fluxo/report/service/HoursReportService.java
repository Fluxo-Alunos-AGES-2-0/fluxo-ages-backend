package com.fluxo.report.service;

import com.fluxo.report.dto.HoursReportDto;
import com.fluxo.report.entity.HoursReport;
import com.fluxo.report.repository.HoursReportRepository;
import com.fluxo.user.entity.User;
import com.fluxo.user.service.StudentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HoursReportService {

    private final HoursReportRepository repository;
    private final StudentService studentService;

    public HoursReportService(HoursReportRepository repository, StudentService studentService) {
        this.repository = repository;
        this.studentService = studentService;
    }

    public List<HoursReportDto> getMyReports(Integer idReport) {
        User user = getAuthenticatedUser();
        Integer projectId = getAuthenticatedProjectId();

        if (projectId == null) {
            return List.of();
        }

        if (idReport != null) {
            HoursReport report = repository
                    .findByIdAndStudentUserIdAndProjectId(idReport, user.getId(), projectId)
                    .orElseThrow(EntityNotFoundException::new);

            return List.of(new HoursReportDto(report));
        }

        return repository
                .findByStudentUserIdAndProjectId(user.getId(), projectId)
                .stream()
                .map(HoursReportDto::new)
                .toList();
    }

    public List<HoursReportDto> getMyHours() {
        return getMyReports(null);
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (User) principal;
    }

    private Integer getAuthenticatedProjectId() {
        return studentService.getLoggedStudentProfile()
                .map(profile -> profile.getCurrentProject().getId())
                .orElse(null);
    }
}