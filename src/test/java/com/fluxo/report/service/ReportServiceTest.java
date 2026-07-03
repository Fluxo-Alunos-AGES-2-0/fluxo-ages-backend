package com.fluxo.report.service;

import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.infra.storage.StorageReferenceResolver;
import com.fluxo.project.entity.Project;
import com.fluxo.project.entity.Team;
import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.dto.ReportArchiveResponseDto;
import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.repository.ReportArchiveRepository;
import com.fluxo.report.repository.ReportRepository;
import com.fluxo.report.repository.ReportReviewRepository;
import com.fluxo.report.repository.SprintReportRepository;
import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.entity.ReportReview;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.repository.UserRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportArchiveRepository reportArchiveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private HoursReportRepository hoursReportRepository;

    @Mock
    private SprintReportRepository sprintReportRepository;

    @Mock
    private ReportReviewRepository reportReviewRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private StorageReferenceResolver storageReferenceResolver;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("getFinalReports resolves the storage URL before returning the response")
    void getFinalReportsResolvesStorageUrl() {
        User authenticatedUser = new User();
        authenticatedUser.setId(7);

        Project project = new Project();
        project.setName("Projeto S3");

        ReportArchive report = new ReportArchive();
        report.setId(15);
        report.setType(ReportType.RF);
        report.setProject(project);
        report.setCreateDate(OffsetDateTime.parse("2026-06-05T10:15:30Z"));
        report.setUrlArchive("s3:reports/report.pdf");

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(reportRepository.findByStudentUserId(7)).thenReturn(List.of(report));
        when(fileStorageService.resolveFileUrl("s3:reports/report.pdf"))
                .thenReturn("https://signed.example/report.pdf");

        List<FinalReportResponseDto> result = reportService.getFinalReports();

        assertEquals(1, result.size());
        assertEquals("https://signed.example/report.pdf", result.getFirst().urlArchive());
        assertNull(result.getFirst().feedback());
        verify(reportReviewRepository).findById(15);
        verify(fileStorageService).resolveFileUrl("s3:reports/report.pdf");
    }

    @Test
    @DisplayName("getProgressReports returns teacher feedback when report review exists")
    void getProgressReportsReturnsTeacherFeedbackWhenReviewExists() {
        User authenticatedUser = new User();
        authenticatedUser.setId(7);

        Project project = new Project();
        project.setName("Projeto S3");

        ReportArchive report = new ReportArchive();
        report.setId(17);
        report.setType(ReportType.RA);
        report.setProject(project);
        report.setCreateDate(OffsetDateTime.parse("2026-06-05T10:15:30Z"));
        report.setUrlArchive("s3:reports/progress.pdf");

        ReportReview review = new ReportReview();
        review.setComment("Boa entrega de andamento...");
        review.setCorrectionUrl("s3:corrections/progress-correction.pdf");
        review.setRevisionDate(OffsetDateTime.parse("2026-06-08T10:15:30Z"));

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(reportArchiveRepository.findByStudentUserIdAndType(7, ReportType.RA)).thenReturn(Optional.of(report));
        when(fileStorageService.resolveFileUrl("s3:reports/progress.pdf"))
                .thenReturn("https://signed.example/progress.pdf");
        when(reportReviewRepository.findById(17)).thenReturn(Optional.of(review));
        when(storageReferenceResolver.resolveForDisplay("s3:corrections/progress-correction.pdf"))
                .thenReturn("https://signed.example/progress-correction.pdf");

        List<ProgressReportResponseDto> result = reportService.getProgressReports();

        assertEquals(1, result.size());
        assertEquals("https://signed.example/progress.pdf", result.getFirst().urlArchive());
        assertEquals("Boa entrega de andamento...", result.getFirst().feedback().comment());
        assertEquals("https://signed.example/progress-correction.pdf", result.getFirst().feedback().correctionUrl());
        assertEquals(OffsetDateTime.parse("2026-06-08T10:15:30Z"), result.getFirst().feedback().revisionDate());

        verify(reportReviewRepository).findById(17);
        verify(storageReferenceResolver).resolveForDisplay("s3:corrections/progress-correction.pdf");
    }

    @Test
    @DisplayName("confirmProgressReportUpload replaces the student's previous progress report across projects")
    void confirmProgressReportUploadReplacesPreviousProgressReportAcrossProjects() {
        Project previousProject = new Project();
        previousProject.setId(10);
        previousProject.setName("Projeto Antigo");

        Project currentProject = new Project();
        currentProject.setId(11);
        currentProject.setName("Projeto Atual");

        Team team = new Team();
        team.setProject(currentProject);

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setTeam(team);

        User student = new User();
        student.setId(7);

        ReportArchive existingReport = new ReportArchive();
        existingReport.setId(17);
        existingReport.setType(ReportType.RA);
        existingReport.setStudentUser(student);
        existingReport.setProject(previousProject);
        existingReport.setCreateDate(OffsetDateTime.parse("2026-06-05T10:15:30Z"));
        existingReport.setEditDate(OffsetDateTime.parse("2026-06-05T10:15:30Z"));
        existingReport.setGrade(BigDecimal.valueOf(9.0));
        existingReport.setUrlArchive("s3:reports/previous-progress.pdf");

        when(studentProfileRepository.findByStudentUserId(7)).thenReturn(Optional.of(studentProfile));
        when(fileStorageService.validateReportFileExists("s3:reports/current-progress.pdf", ReportType.RA, 7))
                .thenReturn("s3:reports/current-progress.pdf");
        when(userRepository.getReferenceById(7)).thenReturn(student);
        when(reportArchiveRepository.findByStudentUserIdAndType(7, ReportType.RA))
                .thenReturn(Optional.of(existingReport));
        when(reportReviewRepository.existsChildByReportId(17)).thenReturn(true);
        when(reportArchiveRepository.save(existingReport)).thenReturn(existingReport);
        when(fileStorageService.resolveFileUrl("s3:reports/current-progress.pdf"))
                .thenReturn("https://signed.example/current-progress.pdf");

        ReportArchiveResponseDto response = reportService.confirmProgressReportUpload("s3:reports/current-progress.pdf", 7);

        assertEquals(17, response.id());
        assertEquals("https://signed.example/current-progress.pdf", response.archiveUrl());
        assertEquals(currentProject, existingReport.getProject());
        assertEquals("s3:reports/current-progress.pdf", existingReport.getUrlArchive());
        assertNull(existingReport.getGrade());

        verify(reportArchiveRepository).findByStudentUserIdAndType(7, ReportType.RA);
        verify(fileStorageService).deleteFile("s3:reports/previous-progress.pdf");
        verify(reportReviewRepository).deleteChildByReportId(17);
        verify(reportArchiveRepository, never()).findByStudentUserIdAndProjectIdAndType(7, 11, ReportType.RA);
    }

    @Test
    @DisplayName("confirmProgressReportUpload creates a new progress report when the student has no previous one")
    void confirmProgressReportUploadCreatesNewReportWhenStudentHasNoPreviousOne() {
        Project currentProject = new Project();
        currentProject.setId(11);
        currentProject.setName("Projeto Atual");

        Team team = new Team();
        team.setProject(currentProject);

        User student = new User();
        student.setId(7);

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setStudentUser(student);
        studentProfile.setTeam(team);

        when(studentProfileRepository.findByStudentUserId(7)).thenReturn(Optional.of(studentProfile));
        when(fileStorageService.validateReportFileExists("s3:reports/current-progress.pdf", ReportType.RA, 7))
                .thenReturn("s3:reports/current-progress.pdf");
        when(userRepository.getReferenceById(7)).thenReturn(student);
        when(reportArchiveRepository.findByStudentUserIdAndType(7, ReportType.RA))
                .thenReturn(Optional.empty());
        when(reportArchiveRepository.save(any(ReportArchive.class)))
                .thenAnswer(invocation -> {
                    ReportArchive report = invocation.getArgument(0);
                    report.setId(31);
                    return report;
                });
        when(fileStorageService.resolveFileUrl("s3:reports/current-progress.pdf"))
                .thenReturn("https://signed.example/current-progress.pdf");

        ReportArchiveResponseDto response = reportService.confirmProgressReportUpload("s3:reports/current-progress.pdf", 7);

        ArgumentCaptor<ReportArchive> reportCaptor = ArgumentCaptor.forClass(ReportArchive.class);

        verify(reportArchiveRepository).save(reportCaptor.capture());

        ReportArchive savedReport = reportCaptor.getValue();
        assertEquals(31, response.id());
        assertEquals("https://signed.example/current-progress.pdf", response.archiveUrl());
        assertEquals(ReportType.RA, savedReport.getType());
        assertEquals(student, savedReport.getStudentUser());
        assertEquals(currentProject, savedReport.getProject());
        assertEquals("s3:reports/current-progress.pdf", savedReport.getUrlArchive());
        assertNotNull(savedReport.getCreateDate());
        assertNotNull(savedReport.getEditDate());

        verify(fileStorageService, never()).deleteFile("s3:reports/current-progress.pdf");
        verify(reportReviewRepository, never()).deleteChildByReportId(31);
    }

    @Test
    @DisplayName("confirmFinalReportUpload replaces only the current project's final report")
    void confirmFinalReportUploadReplacesOnlyCurrentProjectsFinalReport() {
        Project currentProject = new Project();
        currentProject.setId(11);
        currentProject.setName("Projeto Atual");

        Team team = new Team();
        team.setProject(currentProject);

        User student = new User();
        student.setId(7);

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setStudentUser(student);
        studentProfile.setTeam(team);

        ReportArchive existingCurrentReport = new ReportArchive();
        existingCurrentReport.setId(41);
        existingCurrentReport.setType(ReportType.RF);
        existingCurrentReport.setStudentUser(student);
        existingCurrentReport.setProject(currentProject);
        existingCurrentReport.setCreateDate(OffsetDateTime.parse("2026-06-05T10:15:30Z"));
        existingCurrentReport.setEditDate(OffsetDateTime.parse("2026-06-05T10:15:30Z"));
        existingCurrentReport.setGrade(BigDecimal.valueOf(8.5));
        existingCurrentReport.setUrlArchive("s3:reports/final-current.pdf");

        when(studentProfileRepository.findByStudentUserId(7)).thenReturn(Optional.of(studentProfile));
        when(fileStorageService.validateReportFileExists("s3:reports/final-updated.pdf", ReportType.RF, 7))
                .thenReturn("s3:reports/final-updated.pdf");
        when(userRepository.getReferenceById(7)).thenReturn(student);
        when(reportArchiveRepository.findByStudentUserIdAndProjectIdAndType(7, 11, ReportType.RF))
                .thenReturn(Optional.of(existingCurrentReport));
        when(reportArchiveRepository.save(existingCurrentReport)).thenReturn(existingCurrentReport);
        when(fileStorageService.resolveFileUrl("s3:reports/final-updated.pdf"))
                .thenReturn("https://signed.example/final-updated.pdf");

        ReportArchiveResponseDto response = reportService.confirmFinalReportUpload("s3:reports/final-updated.pdf", 7);

        assertEquals(41, response.id());
        assertEquals("https://signed.example/final-updated.pdf", response.archiveUrl());
        assertEquals(currentProject, existingCurrentReport.getProject());
        assertEquals("s3:reports/final-updated.pdf", existingCurrentReport.getUrlArchive());
        assertEquals(BigDecimal.valueOf(8.5), existingCurrentReport.getGrade());

        verify(reportArchiveRepository).findByStudentUserIdAndProjectIdAndType(7, 11, ReportType.RF);
        verify(reportArchiveRepository, never()).findByStudentUserIdAndType(7, ReportType.RF);
        verify(fileStorageService).deleteFile("s3:reports/final-current.pdf");
        verify(reportReviewRepository, never()).deleteChildByReportId(41);
    }

    @Test
    @DisplayName("confirmFinalReportUpload creates a new final report when the current project has no previous file")
    void confirmFinalReportUploadCreatesNewReportWhenCurrentProjectHasNoPreviousFile() {
        Project currentProject = new Project();
        currentProject.setId(11);
        currentProject.setName("Projeto Atual");

        Team team = new Team();
        team.setProject(currentProject);

        User student = new User();
        student.setId(7);

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setStudentUser(student);
        studentProfile.setTeam(team);

        when(studentProfileRepository.findByStudentUserId(7)).thenReturn(Optional.of(studentProfile));
        when(fileStorageService.validateReportFileExists("s3:reports/final-first.pdf", ReportType.RF, 7))
                .thenReturn("s3:reports/final-first.pdf");
        when(userRepository.getReferenceById(7)).thenReturn(student);
        when(reportArchiveRepository.findByStudentUserIdAndProjectIdAndType(7, 11, ReportType.RF))
                .thenReturn(Optional.empty());
        when(reportArchiveRepository.save(any(ReportArchive.class)))
                .thenAnswer(invocation -> {
                    ReportArchive report = invocation.getArgument(0);
                    report.setId(45);
                    return report;
                });
        when(fileStorageService.resolveFileUrl("s3:reports/final-first.pdf"))
                .thenReturn("https://signed.example/final-first.pdf");

        ReportArchiveResponseDto response = reportService.confirmFinalReportUpload("s3:reports/final-first.pdf", 7);

        ArgumentCaptor<ReportArchive> reportCaptor = ArgumentCaptor.forClass(ReportArchive.class);
        verify(reportArchiveRepository).save(reportCaptor.capture());

        ReportArchive savedReport = reportCaptor.getValue();
        assertEquals(45, response.id());
        assertEquals("https://signed.example/final-first.pdf", response.archiveUrl());
        assertEquals(ReportType.RF, savedReport.getType());
        assertEquals(currentProject, savedReport.getProject());
        assertEquals(student, savedReport.getStudentUser());
        assertEquals("s3:reports/final-first.pdf", savedReport.getUrlArchive());
        assertNotNull(savedReport.getCreateDate());
        assertNotNull(savedReport.getEditDate());

        verify(reportArchiveRepository).findByStudentUserIdAndProjectIdAndType(7, 11, ReportType.RF);
        verify(reportArchiveRepository, never()).findByStudentUserIdAndType(7, ReportType.RF);
        verify(fileStorageService, never()).deleteFile("s3:reports/final-first.pdf");
        verify(reportReviewRepository, never()).deleteChildByReportId(45);
    }

    @Test
    @DisplayName("getFinalReports returns teacher feedback when report review exists")
    void getFinalReportsReturnsTeacherFeedbackWhenReviewExists() {
        User authenticatedUser = new User();
        authenticatedUser.setId(7);

        Project project = new Project();
        project.setName("Projeto S3");

        ReportArchive report = new ReportArchive();
        report.setId(16);
        report.setType(ReportType.RF);
        report.setProject(project);
        report.setCreateDate(OffsetDateTime.parse("2026-06-05T10:15:30Z"));
        report.setUrlArchive("s3:reports/final.pdf");

        ReportReview review = new ReportReview();
        review.setComment("Boa entrega final...");
        review.setCorrectionUrl("s3:corrections/final-correction.pdf");
        review.setRevisionDate(OffsetDateTime.parse("2026-06-07T10:15:30Z"));

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(reportRepository.findByStudentUserId(7)).thenReturn(List.of(report));
        when(fileStorageService.resolveFileUrl("s3:reports/final.pdf"))
                .thenReturn("https://signed.example/final.pdf");
        when(reportReviewRepository.findById(16)).thenReturn(Optional.of(review));
        when(storageReferenceResolver.resolveForDisplay("s3:corrections/final-correction.pdf"))
                .thenReturn("https://signed.example/final-correction.pdf");

        List<FinalReportResponseDto> result = reportService.getFinalReports();

        assertEquals(1, result.size());
        assertEquals("https://signed.example/final.pdf", result.getFirst().urlArchive());
        assertEquals("Boa entrega final...", result.getFirst().feedback().comment());
        assertEquals("https://signed.example/final-correction.pdf", result.getFirst().feedback().correctionUrl());
        assertEquals(OffsetDateTime.parse("2026-06-07T10:15:30Z"), result.getFirst().feedback().revisionDate());

        verify(reportReviewRepository).findById(16);
        verify(storageReferenceResolver).resolveForDisplay("s3:corrections/final-correction.pdf");
    }

    @Test
    @DisplayName("getFinalReports resolves feedback links through the shared storage resolver")
    void getFinalReportsResolvesFeedbackThroughSharedStorageResolver() {
        User authenticatedUser = new User();
        authenticatedUser.setId(7);

        Project project = new Project();
        project.setName("Projeto S3");

        ReportArchive report = new ReportArchive();
        report.setId(18);
        report.setType(ReportType.RF);
        report.setProject(project);
        report.setCreateDate(OffsetDateTime.parse("2026-06-05T10:15:30Z"));
        report.setUrlArchive("s3:reports/final.pdf");

        ReportReview review = new ReportReview();
        review.setReportId(18);
        review.setComment("Boa entrega final...");
        review.setCorrectionUrl("s3://fluxo--ages-2.0-alunos/dev/review/T2_20261.pdf");
        review.setRevisionDate(OffsetDateTime.parse("2026-06-07T10:15:30Z"));

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(reportRepository.findByStudentUserId(7)).thenReturn(List.of(report));
        when(fileStorageService.resolveFileUrl("s3:reports/final.pdf"))
                .thenReturn("https://signed.example/final.pdf");
        when(reportReviewRepository.findById(18)).thenReturn(Optional.of(review));
        when(storageReferenceResolver.resolveForDisplay("s3://fluxo--ages-2.0-alunos/dev/review/T2_20261.pdf"))
                .thenReturn("https://signed.example/review.pdf");

        List<FinalReportResponseDto> result = reportService.getFinalReports();

        assertEquals(1, result.size());
        assertEquals("https://signed.example/review.pdf", result.getFirst().feedback().correctionUrl());

        verify(storageReferenceResolver).resolveForDisplay("s3://fluxo--ages-2.0-alunos/dev/review/T2_20261.pdf");
    }

    @Test
    @DisplayName("deleteReport removes the stored file before deleting the database rows")
    void deleteReportRemovesStoredFile() {
        User authenticatedUser = new User();
        authenticatedUser.setId(9);

        User student = new User();
        student.setId(9);

        ReportArchive report = new ReportArchive();
        report.setId(88);
        report.setStudentUser(student);
        report.setUrlArchive("s3:reports/final.pdf");

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(reportRepository.findById(88)).thenReturn(Optional.of(report));
        when(hoursReportRepository.existsChildByReportId(88)).thenReturn(false);
        when(sprintReportRepository.existsChildByReportId(88)).thenReturn(false);
        when(reportReviewRepository.existsChildByReportId(88)).thenReturn(false);
        when(reportArchiveRepository.existsChildByReportId(88)).thenReturn(true);

        reportService.deleteReport(88);

        verify(fileStorageService).deleteFile("s3:reports/final.pdf");
        verify(reportArchiveRepository).deleteChildByReportId(88);
        verify(reportRepository).deleteParentByReportId(88);
        verify(hoursReportRepository, never()).deleteChildByReportId(88);
    }
}
