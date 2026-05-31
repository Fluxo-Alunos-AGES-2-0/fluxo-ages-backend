package com.fluxo.report.service;

import com.fluxo.project.entity.Project;
import com.fluxo.report.dto.*;
import com.fluxo.report.entity.SprintReport;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.exception.ReportNotFoundException;
import com.fluxo.report.repository.SprintReportRepository;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

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
                    newSprintReport.setCreateDate(OffsetDateTime.now());
                    newSprintReport.setStudentUser(authenticatedUser);
                    newSprintReport.setProject(project);
                    newSprintReport.setSprint(sprintValue);
                    return newSprintReport;
                });

        sprintReport.setEditDate(OffsetDateTime.now());
        sprintReport.setPredictedActivity(request.predictedActivity());
        sprintReport.setActivityCompleted(request.activityCompleted());
        sprintReport.setProblemsEncountered(request.problemsEncountered());
        sprintReport.setLearnedLessons(request.learnedLessons());
        sprintReport.setNextSteps(request.nextSteps());

        SprintReport savedSprintReport = sprintReportRepository.save(sprintReport);

        return toResponseDto(savedSprintReport);
    }

    public UpdateSprintReportResponseDto updateSprintReport(Integer id, UpdateSprintReportRequestDto request) {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        SprintReport report = getSprintReportById(id);
        if (!report.getStudentUser().getId().equals(authenticatedUser.getId())) {
            throw new IllegalStateException("Você não tem permissão para editar este relatório.");
        }

        report.setPredictedActivity(request.predictedActivity().trim());
        report.setActivityCompleted(request.activityCompleted().trim());
        report.setProblemsEncountered(request.problemsEncountered().trim());
        report.setLearnedLessons(request.learnedLessons().trim());
        report.setNextSteps(request.nextSteps().trim());
        report.setEditDate(OffsetDateTime.now());

        SprintReport updatedReport = sprintReportRepository.save(report);

        return new UpdateSprintReportResponseDto(
                report.getId(),
                report.getSprint(),
                updatedReport.getPredictedActivity(),
                updatedReport.getActivityCompleted(),
                updatedReport.getProblemsEncountered(),
                updatedReport.getLearnedLessons(),
                updatedReport.getNextSteps(),
                updatedReport.getEditDate()
        );
    }

    public SprintReport getSprintReportById(Integer reportId) {
        SprintReport sprintReport = sprintReportRepository.findSprintReportById(reportId);
        if (sprintReport == null)
            throw new ReportNotFoundException("Relatório de id '" + reportId + "' não foi encontrado");
        return sprintReport;
    }

    public List<SprintReportListResponseDto> getMySprintReports(Integer projectId) {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        List<SprintReport> sprintReports;

        if (projectId == null) {
            sprintReports = sprintReportRepository.findByStudentUserIdAndType(
                    authenticatedUser.getId(),
                    ReportType.SPRINT
            );
        } else {
            sprintReports = sprintReportRepository.findByStudentUserIdAndProjectIdAndType(
                    authenticatedUser.getId(),
                    projectId,
                    ReportType.SPRINT
            );
        }

        return sprintReports.stream()
                .sorted(Comparator.comparingInt(this::getSprintNumber))
                .map(this::toListResponseDto)
                .toList();
    }

    private int getSprintNumber(SprintReport sprintReport) {
        return Integer.parseInt(sprintReport.getSprint().trim());
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

    private SprintReportListResponseDto toListResponseDto(SprintReport sprintReport) {
        return new SprintReportListResponseDto(
                sprintReport.getId(),
                "Sprint " + sprintReport.getSprint().trim(),
                sprintReport.getStudentUser().getName(),
                sprintReport.getCreateDate(),
                sprintReport.getProject().getId()
        );
    }
}