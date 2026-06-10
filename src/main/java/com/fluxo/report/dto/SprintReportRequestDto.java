package com.fluxo.report.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SprintReportRequestDto(
        @NotNull(message = "A sprint é obrigatória")
        @Min(value = 1, message = "A sprint deve ser entre 1 e 5")
        @Max(value = 5, message = "A sprint deve ser entre 1 e 5")
        Integer sprint,

        @NotBlank(message = "As atividades previstas são obrigatórias")
        String predictedActivity,

        @NotBlank(message = "As atividades concluídas são obrigatórias")
        String activityCompleted,

        @NotBlank(message = "Os problemas encontrados são obrigatórios")
        String problemsEncountered,

        @NotBlank(message = "As lições aprendidas são obrigatórias")
        String learnedLessons,

        @NotBlank(message = "Os próximos passos são obrigatórios")
        String nextSteps
) {
}
