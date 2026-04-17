package com.fluxo.student.service;

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
        if (studentProfile == null || studentProfile.getTeam() == null) {
            return Optional.empty();
        }

        Project project = findProjectByTeamId(studentProfile.getTeam().getId());
        if (project == null) {
            return Optional.empty();
        }

        int presences = countAttendanceByStatus(authenticatedUser.getId(), true);
        int absences = countAttendanceByStatus(authenticatedUser.getId(), false);
        int totalClasses = presences + absences;

        String avatarUrl = studentProfile.getAvatarUrl();
        Integer agesLevel = parseAgesLevel(studentProfile.getAgpaPosition());
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

    private StudentProfile findStudentProfileByUserId(Long userId) {
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

    private Project findProjectByTeamId(Integer teamId) {
        List<Project> result = entityManager.createQuery("""
            SELECT p
            FROM Project p
            WHERE p.team.id = :teamId
            """, Project.class)
                .setParameter("teamId", teamId)
                .setMaxResults(1)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    private Integer parseAgesLevel(String agpaPosition) {
        if (agpaPosition == null || agpaPosition.isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(agpaPosition.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Valor de AGES level invalido para o aluno autenticado.");
        }
    }

    private StudentProfileResponseDto.ProfessorDto buildProfessorDto(Project project) {
        if (project.getProfessorUser() == null) {
            return null;
        }

        return new StudentProfileResponseDto.ProfessorDto(
                project.getProfessorUser().getId(),
                project.getProfessorUser().getName()
        );
    }

    private int countAttendanceByStatus(Long userId, boolean status) {
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
}
