package com.fluxo.infra.storage;

import com.fluxo.infra.config.StorageProperties;
import com.fluxo.infra.exception.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class S3StorageService {

    private final StorageProperties storageProperties;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public String buildKey(String relativePath) {
        String normalizedPath = normalizePath(relativePath);
        String keyPrefix = normalizePath(storageProperties.getS3().getKeyPrefix());

        if (!StringUtils.hasText(keyPrefix)) {
            return normalizedPath;
        }

        return keyPrefix + "/" + normalizedPath;
    }

    public void uploadObject(String key, String contentType, byte[] content) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(storageProperties.getS3().getBucket())
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content)
            );
        } catch (RuntimeException e) {
            throw new StorageException("Não foi possível salvar o arquivo no S3.", e);
        }
    }

    public String createPutPresignedUrl(String key, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(storageProperties.getS3().getBucket())
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(storageProperties.getS3().getPresignedUrlDurationMinutes()))
                    .putObjectRequest(putObjectRequest)
                    .build();

            return s3Presigner.presignPutObject(presignRequest)
                    .url()
                    .toString();
        } catch (RuntimeException e) {
            throw new StorageException("Não foi possível gerar a URL de upload no S3.", e);
        }
    }

    public String createGetPresignedUrl(String key) {
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
            throw new StorageException("Não foi possível gerar a URL de acesso no S3.", e);
        }
    }

    public byte[] downloadObject(String key) {
        try {
            return s3Client.getObjectAsBytes(GetObjectRequest.builder()
                            .bucket(storageProperties.getS3().getBucket())
                            .key(key)
                            .build())
                    .asByteArray();
        } catch (RuntimeException e) {
            throw new StorageException("NÃ£o foi possÃ­vel baixar o arquivo do S3.", e);
        }
    }

    public void assertObjectExists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(storageProperties.getS3().getBucket())
                    .key(key)
                    .build());
        } catch (RuntimeException e) {
            throw new StorageException("Arquivo informado não foi encontrado no S3.", e);
        }
    }

    public void deleteObject(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(storageProperties.getS3().getBucket())
                    .key(key)
                    .build());
        } catch (RuntimeException e) {
            throw new StorageException("Não foi possível excluir o arquivo do S3.", e);
        }
    }

    public String normalizePath(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
    }
}
