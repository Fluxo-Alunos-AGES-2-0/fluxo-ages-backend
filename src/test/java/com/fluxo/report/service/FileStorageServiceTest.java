package com.fluxo.report.service;

import com.fluxo.infra.storage.S3StorageService;
import com.fluxo.report.enums.ReportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import com.fluxo.infra.exception.StorageException;
import com.fluxo.report.exception.ReportStorageException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

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

    @Test
    @DisplayName("createReportUploadTarget creates PUT upload target with S3 reference and UUID")
    void createReportUploadTargetCreatesPutUploadTargetWithS3ReferenceAndUuid() {
        when(s3StorageService.buildKey(anyString()))
                .thenAnswer(invocation -> "dev/" + invocation.getArgument(0));

        when(s3StorageService.createPutPresignedUrl(anyString(), eq("application/pdf")))
                .thenReturn("https://example.com/upload-url");

        FileStorageService.UploadTarget result = fileStorageService.createReportUploadTarget(
                ReportType.RA,
                10
        );

        assertEquals("https://example.com/upload-url", result.uploadUrl());
        assertEquals("PUT", result.method());
        assertEquals("application/pdf", result.contentType());

        assertTrue(result.fileReference().startsWith("s3:dev/reports/ra/10/"));
        assertTrue(result.fileReference().endsWith(".pdf"));
        assertTrue(result.fileReference().matches("^s3:dev/reports/ra/10/[0-9a-fA-F\\-]{36}\\.pdf$"));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        verify(s3StorageService).createPutPresignedUrl(
                keyCaptor.capture(),
                eq("application/pdf")
        );

        assertTrue(keyCaptor.getValue().matches("^dev/reports/ra/10/[0-9a-fA-F\\-]{36}\\.pdf$"));
    }

    @Test
    @DisplayName("deleteFile throws ReportStorageException when S3 delete fails")
    void deleteFileThrowsReportStorageExceptionWhenS3DeleteFails() {
        doThrow(new StorageException("Erro ao excluir arquivo no S3.", new RuntimeException()))
                .when(s3StorageService)
                .deleteObject("dev/reports/rf/10/file.pdf");

        ReportStorageException exception = assertThrows(
                ReportStorageException.class,
                () -> fileStorageService.deleteFile("s3:dev/reports/rf/10/file.pdf")
        );

        assertEquals("Não foi possível excluir o arquivo do S3.", exception.getMessage());

        verify(s3StorageService).deleteObject("dev/reports/rf/10/file.pdf");
    }

    @Test
    @DisplayName("createReportUploadTarget throws ReportStorageException when S3 upload URL creation fails")
    void createReportUploadTargetThrowsReportStorageExceptionWhenS3UploadUrlCreationFails() {
        when(s3StorageService.buildKey(anyString()))
                .thenReturn("dev/reports/ra/10/file.pdf");

        when(s3StorageService.createPutPresignedUrl(
                "dev/reports/ra/10/file.pdf",
                "application/pdf"
        )).thenThrow(new StorageException("Erro ao gerar URL de upload.", new RuntimeException()));

        ReportStorageException exception = assertThrows(
                ReportStorageException.class,
                () -> fileStorageService.createReportUploadTarget(ReportType.RA, 10)
        );

        assertEquals("Não foi possível gerar a URL de upload no S3.", exception.getMessage());

        verify(s3StorageService).createPutPresignedUrl(
                "dev/reports/ra/10/file.pdf",
                "application/pdf"
        );
    }
}
