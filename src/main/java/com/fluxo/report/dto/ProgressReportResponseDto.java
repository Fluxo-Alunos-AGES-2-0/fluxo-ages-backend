package com.fluxo.report.dto;

import java.time.LocalDate;

public record ProgressReportResponseDto(
        LocalDate date,
        String project,
        double grade,
        String feedback
) {
}
