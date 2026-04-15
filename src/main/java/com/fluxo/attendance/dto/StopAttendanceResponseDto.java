package com.fluxo.attendance.dto;

import com.fluxo.attendance.entity.AttendanceStatus;

import java.time.Instant;

public record StopAttendanceResponseDto(
        Long id,
        String description,
        Instant startTime,
        Instant endTime,
        Integer sessionTimeSeconds,
        AttendanceStatus status
) {
}
