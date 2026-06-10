package com.fluxo.report.service;

import com.fluxo.infra.storage.S3StorageService;
import com.fluxo.report.enums.ReportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private S3StorageService s3StorageService;

    @InjectMocks
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("deleteFile deletes S3 references using the object key")
    void deleteFileDeletesS3ReferenceUsingObjectKey() {
        fileStorageService.deleteFile("s3:dev/reports/rf/10/file.pdf");

        verify(s3StorageService).deleteObject("dev/reports/rf/10/file.pdf");
    }

    @Test
    @DisplayName("deleteFile ignores external absolute URLs")
    void deleteFileIgnoresExternalAbsoluteUrl() {
        fileStorageService.deleteFile("https://example.com/file.pdf");

        verify(s3StorageService, never()).deleteObject("https://example.com/file.pdf");
    }

    @Test
    @DisplayName("validateReportFileExists accepts valid S3 references")
    void validateReportFileExistsAcceptsS3Reference() {
        when(s3StorageService.normalizePath("dev/reports/ra/10/file.pdf"))
                .thenReturn("dev/reports/ra/10/file.pdf");
        when(s3StorageService.buildKey("reports/ra/10"))
                .thenReturn("dev/reports/ra/10");

        String result = fileStorageService.validateReportFileExists(
                "s3:dev/reports/ra/10/file.pdf",
                ReportType.RA,
                10
        );

        assertEquals("s3:dev/reports/ra/10/file.pdf", result);
        verify(s3StorageService).assertObjectExists("dev/reports/ra/10/file.pdf");
    }
}
