package com.fluxo.report.service;

import com.fluxo.infra.config.StorageProperties;
import com.fluxo.report.exception.ReportStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final String S3_REFERENCE_PREFIX = "s3:";

    private final StorageProperties storageProperties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public String saveReportFile(MultipartFile file) {
        String key = buildS3Key("reports/" + UUID.randomUUID() + ".pdf");

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(storageProperties.getS3().getBucket())
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );

            return S3_REFERENCE_PREFIX + key;
        } catch (IOException e) {
            throw new ReportStorageException("Nao foi possivel ler o arquivo para envio ao S3.", e);
        } catch (RuntimeException e) {
            throw new ReportStorageException("Nao foi possivel salvar o arquivo no S3.", e);
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

        return generatePresignedUrl(key);
    }

    public void deleteFile(String fileReference) {
        if (!StringUtils.hasText(fileReference) || looksLikeAbsoluteUrl(fileReference)) {
            return;
        }

        String key = fileReference.startsWith(S3_REFERENCE_PREFIX)
                ? fileReference.substring(S3_REFERENCE_PREFIX.length())
                : fileReference;

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(storageProperties.getS3().getBucket())
                    .key(key)
                    .build());
        } catch (RuntimeException e) {
            throw new ReportStorageException("Nao foi possivel deletar o arquivo do S3.", e);
        }
    }

    private String generatePresignedUrl(String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(storageProperties.getS3().getBucket())
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(storageProperties.getS3().getPresignedUrlDurationMinutes()))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest)
                    .url()
                    .toString();
        } catch (RuntimeException e) {
            throw new ReportStorageException("Nao foi possivel gerar a URL de acesso no S3.", e);
        }
    }

    private String buildS3Key(String relativePath) {
        String normalizedPath = normalizePath(relativePath);
        String keyPrefix = normalizePath(storageProperties.getS3().getKeyPrefix());

        if (!StringUtils.hasText(keyPrefix)) {
            return normalizedPath;
        }

        return keyPrefix + "/" + normalizedPath;
    }

    private String normalizePath(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private boolean looksLikeAbsoluteUrl(String value) {
        try {
            URI uri = URI.create(value);
            return uri.getScheme() != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
