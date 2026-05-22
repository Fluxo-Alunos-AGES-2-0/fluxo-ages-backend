package com.fluxo.report.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SprintReportRequestDto(
        @NotNull(message = "A sprint e obrigatoria")
        @Min(value = 1, message = "A sprint deve ser entre 1 e 5")
        @Max(value = 5, message = "A sprint deve ser entre 1 e 5")
        Integer sprint,

        @NotBlank(message = "As atividades previstas sao obrigatorias")
        String predictedActivity,

        @NotBlank(message = "As atividades concluidas sao obrigatorias")
        String activityCompleted,

        @NotBlank(message = "Os problemas encontrados sao obrigatorios")
        String problemsEncountered,

        @NotBlank(message = "As licoes aprendidas sao obrigatorias")
        String learnedLessons,

        @NotBlank(message = "Os proximos passos sao obrigatorios")
        String nextSteps
) {
}