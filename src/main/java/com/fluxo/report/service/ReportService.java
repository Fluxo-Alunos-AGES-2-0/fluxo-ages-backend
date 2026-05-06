package com.fluxo.report.service;

import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.entity.Report;
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

    public List<ProgressReportResponseDto> getProgressReport() {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        List<Report> relatorios = reportRepository.findByStudentUserId(authenticatedUser.getId());
        
        // Tratando as diferenças de relatórios usando o Enum 'ReportType' para identificar a categoria
        return relatorios.stream().map(report -> {
            String comment = "";

            if ((report.getType() == ReportType.RA || report.getType() == ReportType.RF) 
                    && report instanceof ReportReview review) {
                comment = review.getComment();
            }

            return new ProgressReportResponseDto(
                    report.getCreateDate(),
                    report.getProject().getName(),
                    report.getGrade(),
                    comment
            );
        }).toList();
    }
}
