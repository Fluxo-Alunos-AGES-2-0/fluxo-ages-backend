package com.fluxo.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinalReportResponseDto(
        LocalDate date,
        String project,
        BigDecimal grade,
        String feedback
) {
}
