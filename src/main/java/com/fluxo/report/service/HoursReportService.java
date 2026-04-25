package com.fluxo.report.service;

import com.fluxo.report.dto.HoursReportDto;
import com.fluxo.report.entity.HoursReport;
import com.fluxo.report.repository.HoursReportRepository;
import com.fluxo.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HoursReportService {

    private final HoursReportRepository repository;

    public HoursReportService(HoursReportRepository repository) {
        this.repository = repository;
    }

    public List<HoursReportDto> getMyReports(Integer idReport) {
        User user = getAuthenticatedUser();

        if (idReport != null) {
            HoursReport report = repository
                    .findByIdAndStudentUserId(idReport, user.getId())
                    .orElseThrow(EntityNotFoundException::new);

            return List.of(new HoursReportDto(report));
        }

        return repository
                .findByStudentUserId(user.getId())
                .stream()
                .map(HoursReportDto::new)
                .toList();
    }

    private User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return (User) principal;
    }
}