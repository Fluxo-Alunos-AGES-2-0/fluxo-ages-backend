package com.fluxo.student.service;

import com.fluxo.academic.entity.AttendanceRecordStatus;
import com.fluxo.project.entity.Project;
import com.fluxo.student.dto.StudentProfileResponseDto;
import com.fluxo.student.entity.StudentProfile;
import com.fluxo.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<StudentProfileResponseDto> getLoggedStudentProfile() {
        User authenticatedUser = getAuthenticatedUser();

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

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado.");
        }

        return user;
    }

    private StudentProfile findStudentProfileByUserId(Integer userId) {
        List<StudentProfile> result = entityManager.createQuery("""
            SELECT sp
            FROM StudentProfile sp
            WHERE sp.studentUser.id = :userId
            """, StudentProfile.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
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
        Long count = entityManager.createQuery("""
            SELECT COUNT(ar)
            FROM AttendanceRecord ar
            WHERE ar.studentUser.id = :userId
              AND ar.status = :status
            """, Long.class)
                .setParameter("userId", userId)
                .setParameter("status", status)
                .getSingleResult();

        return count.intValue();
    }

    private int countAttendanceRecords(Integer userId) {
        Long count = entityManager.createQuery("""
            SELECT COUNT(ar)
            FROM AttendanceRecord ar
            WHERE ar.studentUser.id = :userId
            """, Long.class)
                .setParameter("userId", userId)
                .getSingleResult();

        return count.intValue();
    }
}
