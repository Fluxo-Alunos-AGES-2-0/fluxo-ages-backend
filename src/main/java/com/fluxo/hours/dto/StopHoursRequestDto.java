package com.fluxo.hours.dto;

import jakarta.validation.constraints.Size;

public record StopHoursRequestDto(
        @Size(max = 1250, message = "A descrição não pode ultrapassar 1250 caracteres.")
        String description
) {
}
