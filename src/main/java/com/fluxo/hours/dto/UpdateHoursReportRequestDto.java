package com.fluxo.hours.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;


public record UpdateHoursReportRequestDto(
        @NotNull(message = "Campos obrigatórios não podem ser vazios")
        OffsetDateTime entryTime,

        @NotNull(message = "Campos obrigatórios não podem ser vazios")
        OffsetDateTime exitTime,

        @NotBlank(message = "Campos obrigatórios não podem ser vazios")
        String activities
) {
}
