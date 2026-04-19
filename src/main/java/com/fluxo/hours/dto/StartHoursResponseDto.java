package com.fluxo.hours.dto;

import java.time.Instant;

public record StartHoursResponseDto(
        Integer id,
        Instant startTime
) {
}
