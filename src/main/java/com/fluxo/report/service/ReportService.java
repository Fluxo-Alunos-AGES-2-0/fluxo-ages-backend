package com.fluxo.report.service;

import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.project.entity.Project;
import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.dto.ReportArchiveResponseDto;
import com.fluxo.report.entity.Report;
import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.entity.ReportReview;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.exception.InvalidReportFileException;
import com.fluxo.report.exception.ReportAccessDeniedException;
import com.fluxo.report.exception.ReportNotFoundException;
import com.fluxo.report.exception.StudentProjectNotFoundException;
import com.fluxo.report.repository.ReportArchiveRepository;
import com.fluxo.report.repository.ReportRepository;
import com.fluxo.report.repository.ReportReviewRepository;
import com.fluxo.report.repository.SprintReportRepository;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.UserRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024;
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final ReportArchiveRepository reportArchiveRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ReportRepository reportRepository;
    private final HoursReportRepository hoursReportRepository;
    private final SprintReportRepository sprintReportRepository;
    private final ReportReviewRepository reportReviewRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

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
                        report instanceof ReportReview reportReview ? reportReview.getComment() : null
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
                        report instanceof ReportReview reportReview ? reportReview.getComment() : null
                ))
                .toList();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidReportFileException("Arquivo nao pode estar vazio.");
        }

        if (!PDF_CONTENT_TYPE.equals(file.getContentType())) {
            throw new InvalidReportFileException("Formato invalido. Apenas arquivos PDF sao aceitos.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidReportFileException("O arquivo nao pode exceder 25MB.");
        }
    }

    private Project findCurrentProject(Integer userId) {
        List<com.fluxo.user.entity.StudentProfile> result = entityManager.createQuery(
                        "SELECT sp FROM StudentProfile sp WHERE sp.studentUser.id = :userId",
                        com.fluxo.user.entity.StudentProfile.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        if (result.isEmpty() || result.getFirst().getTeam() == null || result.getFirst().getTeam().getProject() == null) {
            throw new StudentProjectNotFoundException("Projeto atual do aluno nao encontrado.");
        }

        return result.getFirst().getTeam().getProject();
    }

    @FunctionalInterface
    private interface ExistingReportFinder {
        Optional<ReportArchive> find(Integer studentId, Project project);
    }

    private ReportArchiveResponseDto uploadReport(
            MultipartFile file,
            Integer studentId,
            ReportType reportType,
            ExistingReportFinder existingReportFinder) {

        validateFile(file);

        User studentRef = userRepository.getReferenceById(studentId);
        Project projectRef = findCurrentProject(studentId);
        Optional<ReportArchive> existingReportOpt = existingReportFinder.find(studentId, projectRef);

        ReportArchive report;

        if (existingReportOpt.isPresent()) {
            report = existingReportOpt.get();
            fileStorageService.deleteFile(report.getUrlArchive());
            report.setEditDate(OffsetDateTime.now());
        } else {
            report = new ReportArchive();
            report.setStudentUser(studentRef);
            report.setProject(projectRef);
            report.setType(reportType);
            report.setCreateDate(OffsetDateTime.now());
            report.setEditDate(OffsetDateTime.now());
        }

        report.setUrlArchive(fileStorageService.saveReportFile(file));

        ReportArchive savedReport = reportArchiveRepository.save(report);
        return buildResponse(savedReport);
    }

    private ReportArchiveResponseDto buildResponse(ReportArchive report) {
        return new ReportArchiveResponseDto(
                report.getId(),
                fileStorageService.resolveFileUrl(report.getUrlArchive()),
                report.getCreateDate().toInstant().toString()
        );
    }

    @Transactional
    public ReportArchiveResponseDto uploadProgressReport(MultipartFile file, Integer studentId) {
        return uploadReport(
                file,
                studentId,
                ReportType.RA,
                (id, project) -> reportArchiveRepository.findByStudentUserIdAndProjectIdAndType(id, project.getId(), ReportType.RA)
        );
    }

    @Transactional
    public ReportArchiveResponseDto uploadFinalReport(MultipartFile file, Integer studentId) {
        return uploadReport(
                file,
                studentId,
                ReportType.RF,
                (id, project) -> reportArchiveRepository.findByStudentUserIdAndProjectIdAndType(id, project.getId(), ReportType.RF)
        );
    }

    @Transactional
    public void deleteReport(Integer reportId) {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException("Relatorio nao encontrado."));

        if (!report.getStudentUser().getId().equals(authenticatedUser.getId())) {
            throw new ReportAccessDeniedException("Usuario autenticado nao possui permissao para excluir este relatorio.");
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
