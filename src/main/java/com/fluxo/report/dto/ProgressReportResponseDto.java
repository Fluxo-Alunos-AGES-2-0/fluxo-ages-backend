package com.fluxo.report.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ProgressReportResponseDto(
        OffsetDateTime date,
        String project,
        BigDecimal grade,
        String feedback
) {
}
