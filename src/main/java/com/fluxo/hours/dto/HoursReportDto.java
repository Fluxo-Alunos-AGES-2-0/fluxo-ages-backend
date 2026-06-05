package com.fluxo.hours.dto;

public record HoursReportDto(
                Long id,
                String startTime,
                String endTime,
                Integer sessionTimeSeconds,
                String activities,
                String status,
                String rejectionJustification) {
}
