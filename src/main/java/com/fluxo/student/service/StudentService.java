package com.fluxo.student.service;

import com.fluxo.project.entity.Project;
import com.fluxo.student.dto.StudentProfileResponseDto;
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

        Integer teamId = findTeamIdByStudentUserId(authenticatedUser.getId());
        if (teamId == null) {
            return Optional.empty();
        }

        Project project = findProjectByTeamId(teamId);
        if (project == null) {
            return Optional.empty();
        }

        int presences = countAttendanceByStatus(authenticatedUser.getId(), true);
        int absences = countAttendanceByStatus(authenticatedUser.getId(), false);
        int totalClasses = presences + absences;

        // avatarUrl, agesLevel e professor ficam como null por enquanto,
        // pois não foi encontrada uma origem clara para esses dados no projeto.

        String avatarUrl = null;
        Integer agesLevel = null;
        StudentProfileResponseDto.ProfessorDto professor = null;

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
            throw new IllegalStateException("Usuário autenticado não encontrado.");
        }

        return user;
    }

    private Integer findTeamIdByStudentUserId(Long userId) {
        List<Integer> result = entityManager.createQuery("""
            SELECT sp.team.id
            FROM StudentProfile sp
            WHERE sp.studentUser.id = :userId
            """, Integer.class)
                .setParameter("userId", userId)
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