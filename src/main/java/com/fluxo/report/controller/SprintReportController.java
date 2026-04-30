package com.fluxo.report.controller;

import com.fluxo.report.dto.SprintReportRequestDto;
import com.fluxo.report.dto.SprintReportResponseDto;
import com.fluxo.report.service.SprintReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class SprintReportController {

    private final SprintReportService sprintReportService;

    @PostMapping("/sprint")
    public ResponseEntity<SprintReportResponseDto> createSprintReport(
            @Valid @RequestBody SprintReportRequestDto request
    ) {
        SprintReportResponseDto response = sprintReportService.createSprintReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}