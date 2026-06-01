package com.fluxo.report.dto;

import java.time.OffsetDateTime;

public record UpdateSprintReportResponseDto(
        Integer id,
        String sprint,
        String predictedActivity,
        String activityCompleted,
        String problemsEncountered,
        String learnedLessons,
        String nextSteps,
        OffsetDateTime editDate
) {}
