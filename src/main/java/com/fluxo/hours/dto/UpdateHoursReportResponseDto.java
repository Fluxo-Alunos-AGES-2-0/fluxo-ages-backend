package com.fluxo.hours.dto;

import com.fluxo.hours.entity.HoursReportStatus;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record UpdateHoursReportResponseDto(
        Integer id,
        OffsetDateTime entryTime,
        OffsetDateTime exitTime,
        Integer totalTimeSeconds,
        String activities,
        LocalDateTime editDate
) {}
