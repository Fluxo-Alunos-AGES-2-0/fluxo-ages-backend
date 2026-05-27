package com.fluxo.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SprintReportListResponseDto(
        Integer id,
        String sprint,
        String student,
        LocalDateTime date,

        @JsonProperty("id_project")
        Integer projectId
) {
}
