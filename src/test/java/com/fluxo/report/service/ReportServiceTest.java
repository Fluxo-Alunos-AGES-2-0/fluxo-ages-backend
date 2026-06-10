package com.fluxo.report.service;

import com.fluxo.hours.repository.HoursReportRepository;
import com.fluxo.project.entity.Project;
import com.fluxo.report.dto.FinalReportResponseDto;
import com.fluxo.report.entity.ReportArchive;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.repository.ReportArchiveRepository;
import com.fluxo.report.repository.ReportRepository;
import com.fluxo.report.repository.ReportReviewRepository;
import com.fluxo.report.repository.SprintReportRepository;
import com.fluxo.user.entity.User;
import com.fluxo.user.repository.UserRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        verify(fileStorageService).resolveFileUrl("s3:reports/report.pdf");
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
