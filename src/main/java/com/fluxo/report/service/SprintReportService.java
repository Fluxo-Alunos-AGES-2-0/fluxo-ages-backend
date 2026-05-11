package com.fluxo.report.service;

import com.fluxo.project.entity.Project;
import com.fluxo.report.dto.SprintReportRequestDto;
import com.fluxo.report.dto.SprintReportResponseDto;
import com.fluxo.report.entity.SprintReport;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.repository.SprintReportRepository;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SprintReportService {

    private final SprintReportRepository sprintReportRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final StudentProfileRepository studentProfileRepository;

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
        String sprintValue = String.valueOf(request.sprint());

        SprintReport sprintReport = sprintReportRepository
                .findByStudentUserIdAndProjectIdAndSprintAndType(
                        authenticatedUser.getId(),
                        project.getId(),
                        sprintValue,
                        ReportType.SPRINT
                )
                .orElseGet(() -> {
                    SprintReport newSprintReport = new SprintReport();
                    newSprintReport.setType(ReportType.SPRINT);
                    newSprintReport.setCreateDate(LocalDate.now());
                    newSprintReport.setStudentUser(authenticatedUser);
                    newSprintReport.setProject(project);
                    newSprintReport.setSprint(sprintValue);
                    return newSprintReport;
                });

        sprintReport.setEditDate(LocalDate.now());
        sprintReport.setPredictedActivity(request.predictedActivity());
        sprintReport.setActivityCompleted(request.activityCompleted());
        sprintReport.setProblemsEncountered(request.problemsEncountered());
        sprintReport.setLearnedLessons(request.learnedLessons());
        sprintReport.setNextSteps(request.nextSteps());

        SprintReport savedSprintReport = sprintReportRepository.save(sprintReport);

        return toResponseDto(savedSprintReport);
    }

    private StudentProfile findStudentProfileByUserId(Integer userId) {
        return studentProfileRepository.findByStudentUserId(userId).orElse(null);
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