package com.fluxo.report.service;

import com.fluxo.report.dto.ProgressReportResponseDto;
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

        return reportRepository.findProgressReportsByUserId(authenticatedUser.getId());
    }
}
