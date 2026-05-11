package com.fluxo.report.service;

import com.fluxo.project.entity.Project;
import com.fluxo.project.repository.ProjectRepository;
import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.dto.ReportArchiveResponseDto;
import com.fluxo.report.entity.Report;
import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.entity.ReportReview;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.exception.InvalidReportFileException;
import com.fluxo.report.exception.StudentProjectNotFoundException;
import com.fluxo.report.repository.ReportArchiveRepository;
import com.fluxo.report.repository.ReportRepository;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.UserRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final ReportArchiveRepository reportArchiveRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final FileStorageService fileStorageService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ReportRepository reportRepository;

    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public List<ProgressReportResponseDto> getProgressReports() {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        return reportRepository.findByStudentUserId(authenticatedUser.getId())
                .stream()
                .filter(report -> report.getType() == ReportType.RA)
                .map(report -> new ProgressReportResponseDto(
                        report.getCreateDate(),
                        report.getProject().getName(),
                        report.getGrade(),
                        report instanceof ReportReview rr ? rr.getComment() : null
                ))
                .toList();
    }

    public List<FinalReportResponseDto> getFinalReports() {
        User authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        return reportRepository.findByStudentUserId(authenticatedUser.getId())
                .stream()
                .filter(report -> report.getType() == ReportType.RF)
                .map(report -> new FinalReportResponseDto(
                        report.getCreateDate(),
                        report.getProject().getName(),
                        report.getGrade(),
                        report instanceof ReportReview rr ? rr.getComment() : null
                ))
                .toList();
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidReportFileException("Arquivo não pode estar vazio.");
        }

        if (file.getContentType() == null || !file.getContentType().equals(PDF_CONTENT_TYPE)) {
            throw new InvalidReportFileException("Formato inválido. Apenas arquivos PDF são aceitos.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidReportFileException("O arquivo não pode exceder 25MB.");
        }
    }

    private Project findCurrentProject(Integer userId) {
        java.util.List<com.fluxo.user.entity.StudentProfile> result = entityManager.createQuery(
                "SELECT sp FROM StudentProfile sp WHERE sp.studentUser.id = :userId",
                com.fluxo.user.entity.StudentProfile.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();

        if (result.isEmpty() || result.get(0).getTeam() == null || result.get(0).getTeam().getProject() == null) {
            throw new StudentProjectNotFoundException("Projeto atual do aluno não encontrado.");
        }

        return result.get(0).getTeam().getProject();
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

            report.setEditDate(LocalDate.now());
        } else {
            report = new ReportArchive();

            report.setStudentUser(studentRef);
            report.setProject(projectRef);
            report.setType(reportType);
            report.setCreateDate(LocalDate.now());
            report.setEditDate(LocalDate.now());
        }

        String fileUrl = fileStorageService.saveFile(file);
        report.setUrlArchive(fileUrl);

        ReportArchive savedReport = reportArchiveRepository.save(report);

        return buildResponse(savedReport);
    }

    private ReportArchiveResponseDto buildResponse(ReportArchive report) {
        return new ReportArchiveResponseDto(
                report.getId(),
                report.getUrlArchive(),
                report.getCreateDate().atStartOfDay()
                        .atOffset(java.time.ZoneOffset.UTC)
                        .toInstant()
                        .toString()
        );
    }

    @Transactional
    public ReportArchiveResponseDto uploadProgressReport(MultipartFile file, Integer studentId) {
        return uploadReport(
                file,
                studentId,
                ReportType.RA,
                (id, project) -> reportArchiveRepository.findByStudentUserIdAndType(id, ReportType.RA)
        );
    }

    @Transactional
    public ReportArchiveResponseDto uploadFinalReport(MultipartFile file, Integer studentId) {
        Project projectRef = findCurrentProject(studentId);
        return uploadReport(
                file,
                studentId,
                ReportType.RF,
                (id, project) -> reportArchiveRepository.findByStudentUserIdAndProjectIdAndType(id, project.getId(), ReportType.RF)
        );
    }
