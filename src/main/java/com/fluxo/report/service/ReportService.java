package com.fluxo.report.service;

import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.project.entity.Project;
import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.dto.ReportArchiveResponseDto;
import com.fluxo.report.dto.ReportUploadUrlResponseDto;
import com.fluxo.report.dto.ReportFeedbackResponseDto;
import com.fluxo.report.entity.Report;
import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.entity.SprintReport;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.exception.InvalidReportFileException;
import com.fluxo.report.exception.ReportAccessDeniedException;
import com.fluxo.report.exception.ReportNotFoundException;
import com.fluxo.report.exception.StudentProjectNotFoundException;
import com.fluxo.report.repository.ReportArchiveRepository;
import com.fluxo.report.repository.ReportRepository;
import com.fluxo.report.repository.ReportReviewRepository;
import com.fluxo.report.repository.SprintReportRepository;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.repository.UserRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final ReportArchiveRepository reportArchiveRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ReportRepository reportRepository;
    private final HoursReportRepository hoursReportRepository;
    private final SprintReportRepository sprintReportRepository;
    private final ReportReviewRepository reportReviewRepository;
    private final StudentProfileRepository studentProfileRepository;

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidReportFileException("Arquivo não pode estar vazio.");
        }

        String contentType = file.getContentType();
        if (!PDF_CONTENT_TYPE.equals(contentType)) {
            throw new InvalidReportFileException("Arquivo deve ser do tipo PDF.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidReportFileException("Arquivo não pode exceder 25MB.");
        }
    }

    @Transactional
    public ReportArchiveResponseDto uploadProgressReport(MultipartFile file, Integer studentId) {
        validateFile(file);
        Project currentProject = findCurrentProject(studentId);
        String fileReference = fileStorageService.uploadFile(file, ReportType.RA, studentId);
        return buildResponseFromSave(saveReportReference(studentId, currentProject, ReportType.RA, fileReference, 
            (id, project) -> reportArchiveRepository.findByStudentUserIdAndProjectIdAndType(id, project.getId(), ReportType.RA)));
    }

    @Transactional
    public ReportArchiveResponseDto uploadFinalReport(MultipartFile file, Integer studentId) {
        validateFile(file);
        Project currentProject = findCurrentProject(studentId);
        String fileReference = fileStorageService.uploadFile(file, ReportType.RF, studentId);
        return buildResponseFromSave(saveReportReference(studentId, currentProject, ReportType.RF, fileReference,
            (id, project) -> reportArchiveRepository.findByStudentUserIdAndProjectIdAndType(id, project.getId(), ReportType.RF)));
    }

    public List<ProgressReportResponseDto> getProgressReports() {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        return reportRepository.findByStudentUserId(authenticatedUser.getId())
                .stream()
                .filter(report -> report.getType() == ReportType.RA)
                .map(report -> new ProgressReportResponseDto(
                        report.getId(),
                        report.getCreateDate(),
                        report.getProject().getName(),
                        report.getGrade(),
                        report instanceof ReportArchive reportArchive
                                ? fileStorageService.resolveFileUrl(reportArchive.getUrlArchive())
                                : null,
                        buildFeedback(report.getId())
                ))
                .toList();
    }

    public List<FinalReportResponseDto> getFinalReports() {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        return reportRepository.findByStudentUserId(authenticatedUser.getId())
                .stream()
                .filter(report -> report.getType() == ReportType.RF)
                .map(report -> new FinalReportResponseDto(
                        report.getId(),
                        report.getCreateDate(),
                        report.getProject().getName(),
                        report.getGrade(),
                        report instanceof ReportArchive reportArchive
                                ? fileStorageService.resolveFileUrl(reportArchive.getUrlArchive())
                                : null,
                        buildFeedback(report.getId())
                ))
                .toList();
    }

    @Transactional
    public SprintReport createSprintReport(Integer studentId, String sprintNumber, String predictedActivity,
                                           String activityCompleted, String problemsEncountered,
                                           String learnedLessons, String nextSteps) {
        Project currentProject = findCurrentProject(studentId);
        User studentRef = userRepository.getReferenceById(studentId);

        Optional<SprintReport> existingReport = sprintReportRepository
                .findByStudentUserIdAndProjectIdAndSprint(studentId, currentProject.getId(), sprintNumber);

        SprintReport report;
        if (existingReport.isPresent()) {
            report = existingReport.get();
        } else {
            report = new SprintReport();
            report.setStudentUser(studentRef);
            report.setProject(currentProject);
            report.setType(ReportType.SR);
            report.setCreateDate(OffsetDateTime.now());
            report.setSprint(sprintNumber);
        }

        report.setPredictedActivity(predictedActivity);
        report.setActivityCompleted(activityCompleted);
        report.setProblemsEncountered(problemsEncountered);
        report.setLearnedLessons(learnedLessons);
        report.setNextSteps(nextSteps);
        report.setEditDate(OffsetDateTime.now());

        return sprintReportRepository.save(report);
    }

    public List<SprintReport> getMySprintReports(Integer projectId) {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        List<SprintReport> reports;
        if (projectId != null) {
            reports = sprintReportRepository.findByStudentUserIdAndProjectId(authenticatedUser.getId(), projectId);
        } else {
            reports = sprintReportRepository.findByStudentUserId(authenticatedUser.getId());
        }

        return reports.stream()
                .sorted((r1, r2) -> Integer.compare(extractSprintNumber(r1.getSprint()), extractSprintNumber(r2.getSprint())))
                .toList();
    }

    @Transactional
    public void deleteReport(Integer reportId) {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Relatório não encontrado."));

        if (!report.getStudentUser().getId().equals(authenticatedUser.getId())) {
            throw new ReportAccessDeniedException("Usuário autenticado não possui permissão para excluir este relatório.");
        }

        if (report instanceof ReportArchive reportArchive) {
            fileStorageService.deleteFile(reportArchive.getUrlArchive());
        }

        if (hoursReportRepository.existsChildByReportId(reportId)) {
            hoursReportRepository.deleteChildByReportId(reportId);
        }

        if (sprintReportRepository.existsChildByReportId(reportId)) {
            sprintReportRepository.deleteChildByReportId(reportId);
        }

        if (reportReviewRepository.existsChildByReportId(reportId)) {
            reportReviewRepository.deleteChildByReportId(reportId);
        }

        if (reportArchiveRepository.existsChildByReportId(reportId)) {
            reportArchiveRepository.deleteChildByReportId(reportId);
        }

        reportRepository.deleteParentByReportId(reportId);
    }

    private ReportFeedbackResponseDto buildFeedback(Integer reportId) {
        return reportReviewRepository.findById(reportId)
                .map(reportReview -> new ReportFeedbackResponseDto(
                        reportReview.getComment(),
                        fileStorageService.resolveFileUrl(reportReview.getCorrectionUrl()),
                        reportReview.getRevisionDate()
                ))
                .orElse(null);
    }

    private Project findCurrentProject(Integer userId) {
        StudentProfile studentProfile = studentProfileRepository
                .findByStudentUserId(userId)
                .orElse(null);

        if (studentProfile == null || studentProfile.getTeam() == null || studentProfile.getTeam().getProject() == null) {
            throw new StudentProjectNotFoundException("Projeto atual do aluno não encontrado.");
        }

        return studentProfile.getTeam().getProject();
    }

    @FunctionalInterface
    private interface ExistingReportFinder {
        Optional<ReportArchive> find(Integer studentId, Project project);
    }

    private ReportArchive saveReportReference(
            Integer studentId,
            Project projectRef,
            ReportType reportType,
            String fileReference,
            ExistingReportFinder existingReportFinder) {

        User studentRef = userRepository.getReferenceById(studentId);
        Optional<ReportArchive> existingReportOpt = existingReportFinder.find(studentId, projectRef);

        ReportArchive report;
        if (existingReportOpt.isPresent()) {
            report = existingReportOpt.get();
            if (!fileReference.equals(report.getUrlArchive())) {
                fileStorageService.deleteFile(report.getUrlArchive());
            }
            report.setEditDate(OffsetDateTime.now());
        } else {
            report = new ReportArchive();
            report.setStudentUser(studentRef);
            report.setProject(projectRef);
            report.setType(reportType);
            report.setCreateDate(OffsetDateTime.now());
            report.setEditDate(OffsetDateTime.now());
        }

        report.setUrlArchive(fileReference);
        return reportArchiveRepository.save(report);
    }

    private ReportArchiveResponseDto buildResponseFromSave(ReportArchive report) {
        return new ReportArchiveResponseDto(
                report.getId(),
                fileStorageService.resolveFileUrl(report.getUrlArchive()),
                report.getCreateDate().toInstant().toString()
        );
    }

    private int extractSprintNumber(String sprint) {
        try {
            return Integer.parseInt(sprint.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
