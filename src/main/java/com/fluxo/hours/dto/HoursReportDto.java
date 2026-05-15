package com.fluxo.hours.dto;

public record HoursReportDto(
        Long id_report,
        String date,
        String entry_time,
        String exit_time,
        String total_hours,
        String activities,
        String status
) {
}
