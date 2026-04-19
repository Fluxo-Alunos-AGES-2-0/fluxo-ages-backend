package com.fluxo.hours.dto;

import java.time.Instant;

public record StopHoursResponseDto(
        Integer id,
        String description,
        Instant startTime,
        Instant endTime,
        Integer totalTimeSeconds
) {
}
