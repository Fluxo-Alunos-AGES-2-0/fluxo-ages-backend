package com.fluxo.user.dto;

import java.util.List;

public record AttendanceHistoryResponseDto(
        List<AttendanceDayDto> days
) {

    public record AttendanceDayDto(
            String date,
            List<AttendanceSlotDto> slots
    ) {
    }

    public record AttendanceSlotDto(
            String time,
            String status
    ) {
    }
}
