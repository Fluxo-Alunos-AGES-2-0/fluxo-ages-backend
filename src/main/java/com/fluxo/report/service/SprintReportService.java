package com.fluxo.report.service;

import com.fluxo.project.entity.Project;
import com.fluxo.report.dto.SprintReportRequestDto;
import com.fluxo.report.dto.SprintReportResponseDto;
import com.fluxo.report.entity.SprintReport;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.repository.SprintReportRepository;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.service.AuthenticatedUserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SprintReportService {

    private final SprintReportRepository sprintReportRepository;
    private final AuthenticatedUserService authenticatedUserService;

    @PersistenceContext
    private EntityManager entityManager;

    public SprintReportResponseDto createSprintReport(SprintReportRequestDto request) {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        StudentProfile studentProfile = findStudentProfileByUserId(authenticatedUser.getId());
        if (studentProfile == null) {
            throw new IllegalStateException("Perfil do aluno autenticado não encontrado.");
        }

        if (studentProfile.getTeam() == null || studentProfile.getTeam().getProject() == null) {
            throw new IllegalStateException("Projeto do aluno autenticado não encontrado.");
        }

        Project project = studentProfile.getTeam().getProject();

        SprintReport sprintReport = new SprintReport();
        sprintReport.setType(ReportType.SPRINT);
        sprintReport.setCreateDate(LocalDate.now());
        sprintReport.setEditDate(LocalDate.now());
        sprintReport.setStudentUser(authenticatedUser);
        sprintReport.setProject(project);

        sprintReport.setSprint(String.valueOf(request.sprint()));
        sprintReport.setPredictedActivity(request.predictedActivity());
        sprintReport.setActivityCompleted(request.activityCompleted());
        sprintReport.setProblemsEncountered(request.problemsEncountered());
        sprintReport.setLearnedLessons(request.learnedLessons());
        sprintReport.setNextSteps(request.nextSteps());

        SprintReport savedSprintReport = sprintReportRepository.save(sprintReport);

        return toResponseDto(savedSprintReport);
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

    private SprintReportResponseDto toResponseDto(SprintReport sprintReport) {
        return new SprintReportResponseDto(
                sprintReport.getId(),
                Integer.valueOf(sprintReport.getSprint().trim()),
                sprintReport.getPredictedActivity(),
                sprintReport.getActivityCompleted(),
                sprintReport.getProblemsEncountered(),
                sprintReport.getLearnedLessons(),
                sprintReport.getNextSteps()
        );
    }
}