package com.fluxo.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProgressReportResponseDto(
        LocalDate createDate,
        String project,
        BigDecimal grade,
        String comment
) {
}
