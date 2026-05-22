package com.fluxo.hours.dto;

import java.time.Instant;

public record ActiveHoursResponseDto(
        Integer id,
        Instant startTime
) {
}
