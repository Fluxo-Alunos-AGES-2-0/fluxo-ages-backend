package com.fluxo.report.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fluxo.report.service.ReportService;
import com.fluxo.report.dto.ReportArchiveResponseDto;
import com.fluxo.user.entity.User;

@RestController
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping(value = "/progress", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportArchiveResponseDto> uploadProgressReport(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User authenticatedUser = (User) authentication.getPrincipal();
        Integer studentId = authenticatedUser.getId();
        ReportArchiveResponseDto response = reportService.uploadProgressReport(file, studentId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/final", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportArchiveResponseDto> uploadFinalReport(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        User authenticatedUser = (User) authentication.getPrincipal();
        Integer studentId = authenticatedUser.getId();
        ReportArchiveResponseDto response = reportService.uploadFinalReport(file, studentId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}