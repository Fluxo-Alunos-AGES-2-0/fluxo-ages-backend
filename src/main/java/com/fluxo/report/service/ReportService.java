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
import com.fluxo.report.enums.ReportType;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportArchiveRepository reportArchiveRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ReportRepository reportRepository;
    private final HoursReportRepository hoursReportRepository;
    private final SprintReportRepository sprintReportRepository;
    private final ReportReviewRepository reportReviewRepository;
    private final StudentProfileRepository studentProfileRepository;

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

    private ReportArchiveResponseDto buildResponse(ReportArchive report) {
        return new ReportArchiveResponseDto(
                report.getId(),
                fileStorageService.resolveFileUrl(report.getUrlArchive()),
                report.getCreateDate().toInstant().toString()
        );
    }

    public ReportUploadUrlResponseDto createProgressReportUploadUrl(Integer studentId) {
        return createReportUploadUrl(studentId, ReportType.RA);
    }

    public ReportUploadUrlResponseDto createFinalReportUploadUrl(Integer studentId) {
        return createReportUploadUrl(studentId, ReportType.RF);
    }

    private ReportUploadUrlResponseDto createReportUploadUrl(Integer studentId, ReportType reportType) {
        findCurrentProject(studentId);

        FileStorageService.UploadTarget uploadTarget = fileStorageService.createReportUploadTarget(reportType, studentId);

        return new ReportUploadUrlResponseDto(
                uploadTarget.uploadUrl(),
                uploadTarget.fileReference(),
                uploadTarget.method(),
                uploadTarget.contentType()
        );
    }

    @Transactional
    public ReportArchiveResponseDto confirmProgressReportUpload(String fileReference, Integer studentId) {
        return confirmReportUpload(
                fileReference,
                studentId,
                ReportType.RA,
                (id, project) -> reportArchiveRepository.findByStudentUserIdAndProjectIdAndType(id, project.getId(), ReportType.RA)
        );
    }

    @Transactional
    public ReportArchiveResponseDto confirmFinalReportUpload(String fileReference, Integer studentId) {
        return confirmReportUpload(
                fileReference,
                studentId,
                ReportType.RF,
                (id, project) -> reportArchiveRepository.findByStudentUserIdAndProjectIdAndType(id, project.getId(), ReportType.RF)
        );
    }

    private ReportArchiveResponseDto confirmReportUpload(
            String fileReference,
            Integer studentId,
            ReportType reportType,
            ExistingReportFinder existingReportFinder) {

        Project projectRef = findCurrentProject(studentId);
        String canonicalFileReference = fileStorageService.validateReportFileExists(fileReference, reportType, studentId);

        ReportArchive savedReport = saveReportReference(
                studentId,
                projectRef,
                reportType,
                canonicalFileReference,
                existingReportFinder
        );

        return buildResponse(savedReport);
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
}
