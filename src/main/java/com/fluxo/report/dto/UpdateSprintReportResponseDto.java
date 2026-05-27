package com.fluxo.report.dto;

import java.time.LocalDateTime;

public record UpdateSprintReportResponseDto(
        Integer id,
        String sprint,
        String predictedActivity,
        String activityCompleted,
        String problemsEncountered,
        String learnedLessons,
        String nextSteps,
        LocalDateTime editDate
) {}
