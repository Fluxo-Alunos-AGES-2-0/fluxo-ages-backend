package com.fluxo.hours.service;

import com.fluxo.hours.dto.HoursReportDto;
import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.user.entity.User;
import com.fluxo.user.service.StudentService;
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

    public List<HoursReportDto> getMyReports(Integer idProject) {
        User user = getAuthenticatedUser();
        Integer projectId = getAuthenticatedProjectId();

        if (projectId == null) {
            return List.of();
        }

        if (idProject != null) {
            return repository
                    .findByStudentUserIdAndProjectIdAndExitTimeIsNotNullOrderByEntryTimeDesc(user.getId(), idProject)
                    .stream()
                    .map(HoursReportDto::new)
                    .toList();
        }

        return repository
                .findByStudentUserIdAndProjectIdAndExitTimeIsNotNullOrderByEntryTimeDesc(user.getId(), projectId)
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
                .map(profile -> profile.currentProject().id())
                .orElse(null);
    }
}
