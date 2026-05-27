package com.fluxo.report.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinalReportResponseDto(
        LocalDateTime date,
        String project,
        BigDecimal grade,
        String feedback
) {
}
