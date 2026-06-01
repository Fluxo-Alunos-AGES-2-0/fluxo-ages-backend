package com.fluxo.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SprintReportListResponseDto(
        Integer id,
        String sprint,
        String student,
        OffsetDateTime date,

        @JsonProperty("id_project")
        Integer projectId,

        @JsonProperty("predicted_activity")
        String predictedActivity,

        @JsonProperty("activity_completed")
        String activityCompleted,

        @JsonProperty("problems_encountered")
        String problemsEncountered,

        @JsonProperty("learned_lessons")
        String learnedLessons,

        @JsonProperty("next_steps")
        String nextSteps,

        BigDecimal grade
) {
}