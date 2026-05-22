package com.fluxo.user.service;

import com.fluxo.project.entity.Project;
import com.fluxo.user.dto.StudentProfileResponseDto;
import com.fluxo.user.entity.AttendanceRecordStatus;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.AttendanceRecordRepository;
import com.fluxo.user.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final AuthenticatedUserService authenticatedUserService;
    private final StudentProfileRepository studentProfileRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public Optional<StudentProfileResponseDto> getLoggedStudentProfile() {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        StudentProfile studentProfile = findStudentProfileByUserId(authenticatedUser.getId());
        if (studentProfile == null || studentProfile.getTeam() == null || studentProfile.getTeam().getProject() == null) {
            return Optional.empty();
        }

        Project project = studentProfile.getTeam().getProject();

        int presences = countAttendanceByStatus(authenticatedUser.getId(), AttendanceRecordStatus.PRESENTE);
        int absences = countAttendanceByStatus(authenticatedUser.getId(), AttendanceRecordStatus.AUSENTE);
        int totalClasses = countAttendanceRecords(authenticatedUser.getId());

        String avatarUrl = studentProfile.getImageUrl();
        Integer agesLevel = studentProfile.getAgesPosition();
        StudentProfileResponseDto.ProfessorDto professor = buildProfessorDto(project);

        return Optional.of(new StudentProfileResponseDto(
                authenticatedUser.getId(),
                authenticatedUser.getName(),
                authenticatedUser.getEmail(),
                avatarUrl,
                agesLevel,
                new StudentProfileResponseDto.CurrentProjectDto(
                        project.getId(),
                        project.getName()
                ),
                professor,
                new StudentProfileResponseDto.AttendanceDto(
                        totalClasses,
                        presences,
                        absences
                )
        ));
    }

    private StudentProfile findStudentProfileByUserId(Integer userId) {
        return studentProfileRepository.findByStudentUserId(userId).orElse(null);
    }

    private StudentProfileResponseDto.ProfessorDto buildProfessorDto(Project project) {
        if (project.getTeacherUser() == null) {
            return null;
        }

        return new StudentProfileResponseDto.ProfessorDto(
                project.getTeacherUser().getId(),
                project.getTeacherUser().getName()
        );
    }

    private int countAttendanceByStatus(Integer userId, AttendanceRecordStatus status) {
        return attendanceRecordRepository.countByStudentUserIdAndStatus(userId, status);
    }

    private int countAttendanceRecords(Integer userId) {
        return attendanceRecordRepository.countByStudentUserId(userId);
    }
}
