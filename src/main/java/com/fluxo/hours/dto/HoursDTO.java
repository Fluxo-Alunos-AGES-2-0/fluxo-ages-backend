package com.fluxo.hours.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HoursDTO {
    private long completedSeconds;
    private long remainingSeconds;
    private long totalSeconds;
    private double percentual;
}
