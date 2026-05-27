package com.fluxo.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProgressReportResponseDto(
        LocalDateTime date,
        String project,
        BigDecimal grade,
        String feedback
) {
}
