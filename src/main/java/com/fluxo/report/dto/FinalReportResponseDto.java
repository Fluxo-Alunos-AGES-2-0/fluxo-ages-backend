package com.fluxo.report.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record FinalReportResponseDto(
        Integer id,
        OffsetDateTime date,
        String project,
        BigDecimal grade,
        String urlArchive,
        ReportFeedbackResponseDto feedback
) {
}
