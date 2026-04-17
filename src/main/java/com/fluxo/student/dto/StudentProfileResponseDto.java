package com.fluxo.student.dto;

public record StudentProfileResponseDto(
        Long id,
        String name,
        String email,
        String avatarUrl,
        Integer agesLevel,
        CurrentProjectDto currentProject,
        ProfessorDto professor,
        AttendanceDto attendance
) {

    public record CurrentProjectDto(
            Integer id,
            String name
    ) {
    }

    public record ProfessorDto(
            Long id,
            String name
    ) {
    }

    public record AttendanceDto(
            Integer totalClasses,
            Integer presences,
            Integer absences
    ) {
    }
}