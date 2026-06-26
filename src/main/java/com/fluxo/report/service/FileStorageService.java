package com.fluxo.report.service;

import com.fluxo.infra.exception.StorageException;
import com.fluxo.infra.storage.S3StorageService;
import com.fluxo.report.enums.ReportType;
import com.fluxo.report.exception.InvalidReportFileException;
import com.fluxo.report.exception.ReportStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final String S3_REFERENCE_PREFIX = "s3:";
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024; // 25MB

    private final S3StorageService s3StorageService;

    public record UploadTarget(String uploadUrl, String fileReference, String method, String contentType) {
    }

    public String uploadFile(MultipartFile file, ReportType reportType, Integer studentId) {
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

        String key = s3StorageService.buildKey(buildReportRelativePath(reportType, studentId));

        try {
            s3StorageService.uploadFile(key, file.getInputStream(), contentType, file.getSize());
            return S3_REFERENCE_PREFIX + key;
        } catch (IOException e) {
            throw new ReportStorageException("Erro ao fazer upload do arquivo.", e);
        } catch (StorageException e) {
            throw new ReportStorageException("Não foi possível fazer upload do arquivo para o S3.", e);
        }
    }

    public record UploadTarget(String uploadUrl, String fileReference, String method, String contentType) {
    }

    public UploadTarget createReportUploadTarget(ReportType reportType, Integer studentId) {
        String key = s3StorageService.buildKey(buildReportRelativePath(reportType, studentId));

        try {
            String uploadUrl = s3StorageService.createPutPresignedUrl(key, PDF_CONTENT_TYPE);
            return new UploadTarget(uploadUrl, S3_REFERENCE_PREFIX + key, "PUT", PDF_CONTENT_TYPE);
        } catch (StorageException e) {
            throw new ReportStorageException("Não foi possível gerar a URL de upload no S3.", e);
        }
    }

    public String resolveFileUrl(String fileReference) {
        if (!StringUtils.hasText(fileReference)) {
            return fileReference;
        }

        if (looksLikeAbsoluteUrl(fileReference)) {
            return fileReference;
        }

        String key = fileReference.startsWith(S3_REFERENCE_PREFIX)
                ? fileReference.substring(S3_REFERENCE_PREFIX.length())
                : fileReference;

        try {
            return s3StorageService.createGetPresignedUrl(key);
        } catch (StorageException e) {
            throw new ReportStorageException("Não foi possível gerar a URL de acesso no S3.", e);
        }
    }

    public void deleteFile(String fileReference) {
        if (!StringUtils.hasText(fileReference) || looksLikeAbsoluteUrl(fileReference)) {
            return;
        }

        String key = fileReference.startsWith(S3_REFERENCE_PREFIX)
                ? fileReference.substring(S3_REFERENCE_PREFIX.length())
                : fileReference;

        try {
            s3StorageService.deleteObject(key);
        } catch (StorageException e) {
            throw new ReportStorageException("Não foi possível excluir o arquivo do S3.", e);
        }
    }

    public String validateReportFileExists(String fileReference, ReportType reportType, Integer studentId) {
        String key = extractS3Key(fileReference);

        if (!isExpectedReportKey(key, reportType, studentId)) {
            throw new ReportStorageException("Referência de arquivo inválida para relatório.");
        }

        try {
            s3StorageService.assertObjectExists(key);
            return S3_REFERENCE_PREFIX + key;
        } catch (StorageException e) {
            throw new ReportStorageException("Arquivo informado não foi encontrado no S3.", e);
        }
    }

    private String buildReportRelativePath(ReportType reportType, Integer studentId) {
        String typeSegment = reportType == null
                ? "legacy"
                : reportType.name().toLowerCase(Locale.ROOT);
        String ownerSegment = studentId == null ? "unknown" : studentId.toString();

        return "reports/" + typeSegment + "/" + ownerSegment + "/" + UUID.randomUUID() + ".pdf";
    }

    private String extractS3Key(String fileReference) {
        if (!StringUtils.hasText(fileReference) || looksLikeAbsoluteUrl(fileReference)) {
            throw new ReportStorageException("Referência de arquivo inválida para S3.");
        }

        return fileReference.startsWith(S3_REFERENCE_PREFIX)
                ? fileReference.substring(S3_REFERENCE_PREFIX.length())
                : fileReference;
    }

    private boolean isExpectedReportKey(String key, ReportType reportType, Integer studentId) {
        String normalizedKey = s3StorageService.normalizePath(key);
        String expectedPrefix = s3StorageService.buildKey(buildReportRelativePathPrefix(reportType, studentId));

        return normalizedKey.startsWith(expectedPrefix + "/") && normalizedKey.endsWith(".pdf");
    }

    private String buildReportRelativePathPrefix(ReportType reportType, Integer studentId) {
        String typeSegment = reportType == null
                ? "legacy"
                : reportType.name().toLowerCase(Locale.ROOT);
        String ownerSegment = studentId == null ? "unknown" : studentId.toString();

        return "reports/" + typeSegment + "/" + ownerSegment;
    }

    private boolean looksLikeAbsoluteUrl(String value) {
        if (value.startsWith(S3_REFERENCE_PREFIX)) {
            return false;
        }

        try {
            URI uri = URI.create(value);
            return uri.getScheme() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
