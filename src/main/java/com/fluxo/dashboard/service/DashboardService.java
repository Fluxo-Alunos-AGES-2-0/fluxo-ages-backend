package com.fluxo.dashboard.service;

import com.fluxo.dashboard.dto.AttendanceDTO;
import com.fluxo.dashboard.dto.AuxDTO;
import com.fluxo.dashboard.dto.DashboardResponseDTO;
import com.fluxo.dashboard.dto.ProfileDTO;
import com.fluxo.dashboard.dto.ProjectDTO;
import com.fluxo.hours.dto.HoursDTO;
import com.fluxo.hours.service.HoursService;
import com.fluxo.user.dto.StudentProfileResponseDto;
import com.fluxo.user.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final long TOTAL_SECONDS = 216000L;

    private final StudentService studentService;
    private final HoursService hoursService;

    public DashboardResponseDTO getDashboard(Integer userId) {
        StudentProfileResponseDto studentProfile = studentService.getLoggedStudentProfile()
                .orElseThrow(() -> new IllegalStateException("Perfil do aluno nao encontrado"));

        HoursDTO hours = hoursService.getHourControl();

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
