package com.fluxo.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record SprintReportListResponseDto(
        String sprint,
        String student,
        LocalDate date,

        @JsonProperty("id_project")
        Integer projectId
) {
}
