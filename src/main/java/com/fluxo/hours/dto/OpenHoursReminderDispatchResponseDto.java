package com.fluxo.hours.dto;

public record OpenHoursReminderDispatchResponseDto(
        int matchedSessions,
        int sentEmails
) {
}
