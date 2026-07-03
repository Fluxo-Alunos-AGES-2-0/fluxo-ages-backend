package com.fluxo.report.service;

import com.fluxo.infra.storage.S3StorageService;
import com.fluxo.report.exception.ReportTemplateNotFoundException;
import com.fluxo.user.entity.StudentProfile;
import com.fluxo.user.repository.StudentProfileRepository;
import com.fluxo.user.service.AuthenticatedUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportTemplateServiceTest {

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private ReportTemplateService reportTemplateService;

    @Test
    @DisplayName("loadReportTemplate resolves the S3 template for the logged student's AGES level")
    void loadReportTemplateResolvesS3TemplateForStudentLevel() {
        StudentProfile profile = new StudentProfile();
        profile.setAgesPosition(4);

        byte[] content = "template".getBytes(StandardCharsets.UTF_8);

        when(authenticatedUserService.getUserId()).thenReturn(7);
        when(studentProfileRepository.findByStudentUserId(7)).thenReturn(Optional.of(profile));
        when(s3StorageService.buildKey("templates/ages-IV-template.docx"))
                .thenReturn("dev/templates/ages-IV-template.docx");
        when(s3StorageService.downloadObject("dev/templates/ages-IV-template.docx")).thenReturn(content);

        ReportTemplateService.ReportTemplateFile result = reportTemplateService.loadReportTemplate();

        assertEquals("ages-IV-template.docx", result.filename());
        assertArrayEquals(content, result.content());
        verify(s3StorageService).assertObjectExists("dev/templates/ages-IV-template.docx");
        verify(s3StorageService).downloadObject("dev/templates/ages-IV-template.docx");
    }

    @Test
    @DisplayName("loadReportTemplate throws when the student AGES level is invalid")
    void loadReportTemplateThrowsWhenAgesLevelIsInvalid() {
        StudentProfile profile = new StudentProfile();
        profile.setAgesPosition(7);

        when(authenticatedUserService.getUserId()).thenReturn(7);
        when(studentProfileRepository.findByStudentUserId(7)).thenReturn(Optional.of(profile));

        assertThrows(ReportTemplateNotFoundException.class, () -> reportTemplateService.loadReportTemplate());
    }
}
