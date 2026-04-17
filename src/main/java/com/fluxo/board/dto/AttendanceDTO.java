package com.fluxo.board.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder

public class AttendanceDTO {
    private long totalClasses;
    private long presences;
    private long absences;
}
