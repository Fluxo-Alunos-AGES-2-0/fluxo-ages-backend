package com.fluxo.report.service;

import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.entity.ReportReview;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.repository.ReportRepository;
import com.fluxo.user.entity.User;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final AuthenticatedUserService authenticatedUserService;
    private final ReportRepository reportRepository;

    public List<ProgressReportResponseDto> getProgressReports() {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        return reportRepository.findByStudentUserId(authenticatedUser.getId())
                .stream()
                .filter(report -> report.getType() == ReportType.RA)
                .map(report -> new ProgressReportResponseDto(
                        report.getCreateDate(),
                        report.getProject().getName(),
                        report.getGrade(),
                        report instanceof ReportReview rr ? rr.getComment() : null
                ))
                .toList();
    }

    public List<FinalReportResponseDto> getFinalReports() {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        return reportRepository.findByStudentUserId(authenticatedUser.getId())
                .stream()
                .filter(report -> report.getType() == ReportType.RF)
                .map(report -> new FinalReportResponseDto(
                        report.getCreateDate(),
                        report.getProject().getName(),
                        report.getGrade(),
                        report instanceof ReportReview rr ? rr.getComment() : null
                ))
                .toList();
    }

}
