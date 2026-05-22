package com.fluxo.report.dto;

public record SprintReportResponseDto(
        Integer id,
        Integer sprint,
        String predictedActivity,
        String activityCompleted,
        String problemsEncountered,
        String learnedLessons,
        String nextSteps
) {
}