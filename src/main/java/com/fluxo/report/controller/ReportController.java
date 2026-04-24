package com.fluxo.report.controller;

import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/me/progress")
    public ResponseEntity<List<ProgressReportResponseDto>> getMyProgressReports() {
        return ResponseEntity.ok(reportService.getProgressReport());
    }
}
