package com.fluxo.hours.dto;

import jakarta.validation.constraints.Size;

public record StopHoursRequestDto(
        @Size(min = 15, max = 1250, message = "A descrição deve ter entre 15 e 1250 caracteres.")
        String description
) {
}
