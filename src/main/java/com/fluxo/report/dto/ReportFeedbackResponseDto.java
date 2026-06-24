package com.fluxo.report.dto;

import java.time.OffsetDateTime;

public record ReportFeedbackResponseDto(
        String comment,
        String correctionUrl,
        OffsetDateTime revisionDate,
        String teacherName
) {
}