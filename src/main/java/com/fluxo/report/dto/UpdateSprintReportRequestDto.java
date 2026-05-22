package com.fluxo.report.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSprintReportRequestDto(
       @NotBlank String predictedActivity,
       @NotBlank String activityCompleted,
       @NotBlank String problemsEncountered,
       @NotBlank String learnedLessons,
       @NotBlank String nextSteps
) {
}
