package com.fluxo.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSprintReportRequestDto(
        @NotNull(message = "Campos obrigatórios não podem ser vazios")
        @NotBlank(message = "Campos obrigatórios não podem ser vazios")
        String predictedActivity,

        @NotNull(message = "Campos obrigatórios não podem ser vazios")
        @NotBlank(message = "Campos obrigatórios não podem ser vazios")
        String activityCompleted,

        @NotNull(message = "Campos obrigatórios não podem ser vazios")
        @NotBlank(message = "Campos obrigatórios não podem ser vazios")
        String problemsEncountered,

        @NotNull(message = "Campos obrigatórios não podem ser vazios")
        @NotBlank(message = "Campos obrigatórios não podem ser vazios")
        String learnedLessons,

        @NotNull(message = "Campos obrigatórios não podem ser vazios")
        @NotBlank(message = "Campos obrigatórios não podem ser vazios")
        String nextSteps
) {
}
