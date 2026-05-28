package com.fluxo.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProgressReportResponseDto(
        LocalDate date,
        String project,
        BigDecimal grade,
        String feedback
) {
}
