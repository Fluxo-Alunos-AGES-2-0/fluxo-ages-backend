package com.fluxo.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDTO {
    private Integer id;
    private String event;
    private String eventDate;
    private String eventTime;
    private String eventPeriod;
}
