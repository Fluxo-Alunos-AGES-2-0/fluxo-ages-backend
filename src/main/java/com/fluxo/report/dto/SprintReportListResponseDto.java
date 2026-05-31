package com.fluxo.report.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record SprintReportListResponseDto(
        Integer id,
        String sprint,
        String student,
        OffsetDateTime date,

        @JsonProperty("id_project")
        Integer projectId
) {
}
