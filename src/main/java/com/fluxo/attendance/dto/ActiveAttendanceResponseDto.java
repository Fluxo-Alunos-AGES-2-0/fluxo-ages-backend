package com.fluxo.attendance.dto;

import java.time.Instant;

public record ActiveAttendanceResponseDto(
        Long id,
        Instant startTime
) {
}