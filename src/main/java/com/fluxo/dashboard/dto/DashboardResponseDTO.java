package com.fluxo.dashboard.dto;

import com.fluxo.hours.dto.HoursDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponseDTO {
    private ProfileDTO profile;
    private HoursDTO hours;
}
