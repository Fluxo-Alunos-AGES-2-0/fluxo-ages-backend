package com.fluxo.report.service;

import com.fluxo.project.entity.Project;
import com.fluxo.project.entity.Team;
import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.dto.ProgressReportResponseDto;
import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.entity.ReportReview;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.repository.ReportArchiveRepository;
import com.fluxo.report.repository.ReportRepository;
import com.fluxo.report.repository.ReportReviewRepository;
import com.fluxo.report.repository.SprintReportRepository;
import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.UserRepository;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReportService Unit Tests")
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

    @InjectMocks
    private ReportService reportService;

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = new User();
        authenticatedUser.setId(7);
    }

    @Test
    @DisplayName("getFinalReports resolves the storage URL before returning the response")
    void getFinalReportsResolvesStorageUrl() {
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
        verify(fileStorageService).resolveFileUrl("s3:reports/report.pdf");
    }

    @Test
    @DisplayName("getProgressReports returns teacher feedback when report review exists")
    void getProgressReportsReturnsTeacherFeedbackWhenReviewExists() {
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
        when(reportRepository.findByStudentUserId(7)).thenReturn(List.of(report));
        when(fileStorageService.resolveFileUrl("s3:reports/progress.pdf"))
                .thenReturn("https://signed.example/progress.pdf");
        when(reportReviewRepository.findById(17)).thenReturn(Optional.of(review));
        when(fileStorageService.resolveFileUrl("s3:corrections/progress-correction.pdf"))
                .thenReturn("https://signed.example/progress-correction.pdf");

        List<ProgressReportResponseDto> result = reportService.getProgressReports();

        assertEquals(1, result.size());
        assertEquals("https://signed.example/progress.pdf", result.getFirst().urlArchive());
        assertEquals("Boa entrega de andamento...", result.getFirst().feedback().comment());
        assertEquals("https://signed.example/progress-correction.pdf", result.getFirst().feedback().correctionUrl());
        assertEquals(OffsetDateTime.parse("2026-06-08T10:15:30Z"), result.getFirst().feedback().revisionDate());

        verify(reportReviewRepository).findById(17);
        verify(fileStorageService).resolveFileUrl("s3:corrections/progress-correction.pdf");
    }

    @Test
    @DisplayName("getFinalReports returns teacher feedback when report review exists")
    void getFinalReportsReturnsTeacherFeedbackWhenReviewExists() {
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
        when(fileStorageService.resolveFileUrl("s3:corrections/final-correction.pdf"))
                .thenReturn("https://signed.example/final-correction.pdf");

        List<FinalReportResponseDto> result = reportService.getFinalReports();

        assertEquals(1, result.size());
        assertEquals("https://signed.example/final.pdf", result.getFirst().urlArchive());
        assertEquals("Boa entrega final...", result.getFirst().feedback().comment());
        assertEquals("https://signed.example/final-correction.pdf", result.getFirst().feedback().correctionUrl());
        assertEquals(OffsetDateTime.parse("2026-06-07T10:15:30Z"), result.getFirst().feedback().revisionDate());

        verify(reportReviewRepository).findById(16);
        verify(fileStorageService).resolveFileUrl("s3:corrections/final-correction.pdf");
    }

    @Test
    @DisplayName("deleteReport removes the stored file before deleting the database rows")
    void deleteReportRemovesStoredFile() {
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

        // Update authenticated user for access check
        authenticatedUser.setId(9);
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authenticatedUser);

        reportService.deleteReport(88);

        verify(fileStorageService).deleteFile("s3:reports/final.pdf");
        verify(reportArchiveRepository).deleteChildByReportId(88);
        verify(reportRepository).deleteParentByReportId(88);
        verify(hoursReportRepository, never()).deleteChildByReportId(88);
    }
}
