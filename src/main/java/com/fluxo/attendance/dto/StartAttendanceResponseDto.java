package com.fluxo.attendance.dto;

import com.fluxo.attendance.entity.AttendanceStatus;

import java.time.Instant;

public record StartAttendanceResponseDto(
        Long id,
        Instant startTime,
        AttendanceStatus status
) {
}