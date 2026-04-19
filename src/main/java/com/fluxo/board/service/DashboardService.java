package com.fluxo.board.service;

import com.fluxo.attendance.entity.Attendance;
import com.fluxo.attendance.entity.AttendanceStatus;
import com.fluxo.attendance.repository.AttendanceRepository;
import com.fluxo.board.dto.*;
import com.fluxo.student.dto.StudentProfileResponseDto;
import com.fluxo.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final long TOTAL_SECONDS = 216000L;

    private final StudentService studentService;
    private final AttendanceRepository attendanceRepository;

    public DashboardResponseDTO getDashboard(Integer userId) {
        StudentProfileResponseDto studentProfile = studentService.getLoggedStudentProfile()
                .orElseThrow(() -> new IllegalStateException("Perfil do aluno nao encontrado"));

        List<Attendance> approved = attendanceRepository
                .findByStudentUserIdAndStatus(userId, AttendanceStatus.APROVADO);

        long completed = approved.stream()
                .mapToLong(a -> a.getSessionTimeSeconds() != null ? a.getSessionTimeSeconds() : 0L)
                .sum();

        long remaining = Math.max(0L, TOTAL_SECONDS - completed);
        double percentual = (completed * 100.0) / TOTAL_SECONDS;

        HoursDTO hours = HoursDTO.builder()
                .completedSeconds(completed)
                .remainingSeconds(remaining)
                .totalSeconds(TOTAL_SECONDS)
                .percentual(percentual)
                .build();

        AttendanceDTO attendanceDTO = AttendanceDTO.builder()
                .totalClasses(studentProfile.attendance().totalClasses())
                .presences(studentProfile.attendance().presences())
                .absences(studentProfile.attendance().absences())
                .build();

        ProjectDTO projectDTO = studentProfile.currentProject() != null
                ? new ProjectDTO(studentProfile.currentProject().id(), studentProfile.currentProject().name())
                : null;

        AuxDTO professorDTO = studentProfile.professor() != null
                ? new AuxDTO(studentProfile.professor().id(), studentProfile.professor().name())
                : null;

        ProfileDTO profile = ProfileDTO.builder()
                .id(studentProfile.id())
                .name(studentProfile.name())
                .email(studentProfile.email())
                .avatarUrl(studentProfile.avatarUrl())
                .agesLevel(studentProfile.agesLevel())
                .currentProject(projectDTO)
                .professor(professorDTO)
                .attendance(attendanceDTO)
                .build();

        return new DashboardResponseDTO(profile, hours);
    }
}
