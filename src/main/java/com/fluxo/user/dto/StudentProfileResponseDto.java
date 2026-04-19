package com.fluxo.user.dto;

public record StudentProfileResponseDto(
        Integer id,
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
            Integer id,
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
