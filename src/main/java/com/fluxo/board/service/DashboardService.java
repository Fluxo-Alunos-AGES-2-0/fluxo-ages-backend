package com.fluxo.board.service;

import com.fluxo.board.dto.*;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private long TOTAL_SECONDS = 216000;

    public DashboardResponseDTO getDashboard(Long userId) {

        // All the subject are mocked
        long completed = 108000;
        long remaining = TOTAL_SECONDS - completed;
        double percentual = (completed * 100.0) / TOTAL_SECONDS;

        HoursDTO hours = HoursDTO.builder()
                .completedSeconds(completed)
                .remainingSeconds(remaining)
                .totalSeconds(TOTAL_SECONDS)
                .percentual(percentual)
                .build();

        AttendanceDTO attendance = AttendanceDTO.builder()
                .totalClasses(20)
                .presences(17)
                .absences(3)
                .build();

        ProfileDTO profile = ProfileDTO.builder()
                .id(userId)
                .name("João Silva")
                .email("joao.silva@edu.br")
                .avatarUrl("https://example.com/avatar.jpg")
                .agesLevel(2)
                .currentProject(new ProjectDTO(1L, "Fluxo Ages 2.0"))
                .professor(new AuxDTO(1L, "Prof. Dilnei Venturini"))
                .attendance(attendance)
                .build();

        return new DashboardResponseDTO(profile, hours);
    }
}