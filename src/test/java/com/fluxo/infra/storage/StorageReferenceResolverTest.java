package com.fluxo.infra.storage;

import com.fluxo.infra.config.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageReferenceResolverTest {

    @Mock
    private S3StorageService s3StorageService;

    private StorageProperties storageProperties;

    private StorageReferenceResolver storageReferenceResolver;

    @BeforeEach
    void setUp() {
        storageProperties = new StorageProperties();
        storageProperties.getS3().setBucket("fluxo--ages-2.0-alunos");
        storageReferenceResolver = new StorageReferenceResolver(s3StorageService, storageProperties);
    }

    @Test
    @DisplayName("resolveForDisplay presigns canonical s3 references")
    void resolveForDisplayPresignsCanonicalS3References() {
        when(s3StorageService.normalizePath("dev/icones/react.png")).thenReturn("dev/icones/react.png");
        when(s3StorageService.createGetPresignedUrl("dev/icones/react.png")).thenReturn("https://signed/react.png");

        String result = storageReferenceResolver.resolveForDisplay("s3:dev/icones/react.png");

        assertEquals("https://signed/react.png", result);
        verify(s3StorageService).createGetPresignedUrl("dev/icones/react.png");
    }

    @Test
    @DisplayName("resolveForDisplay presigns s3 uri references")
    void resolveForDisplayPresignsS3UriReferences() {
        when(s3StorageService.normalizePath("/dev/icones/postgres.png")).thenReturn("dev/icones/postgres.png");
        when(s3StorageService.createGetPresignedUrl("dev/icones/postgres.png"))
                .thenReturn("https://signed/postgres.png");

        String result = storageReferenceResolver.resolveForDisplay(
                "s3://fluxo--ages-2.0-alunos/dev/icones/postgres.png"
        );

        assertEquals("https://signed/postgres.png", result);
        verify(s3StorageService).createGetPresignedUrl("dev/icones/postgres.png");
    }

    @Test
    @DisplayName("resolveForDisplay presigns path style s3 https urls")
    void resolveForDisplayPresignsPathStyleS3HttpsUrls() {
        when(s3StorageService.normalizePath("/fluxo--ages-2.0-alunos/dev/icones/spring.png"))
                .thenReturn("fluxo--ages-2.0-alunos/dev/icones/spring.png");
        when(s3StorageService.normalizePath("dev/icones/spring.png")).thenReturn("dev/icones/spring.png");
        when(s3StorageService.createGetPresignedUrl("dev/icones/spring.png"))
                .thenReturn("https://signed/spring.png");

        String result = storageReferenceResolver.resolveForDisplay(
                "https://s3.us-east-2.amazonaws.com/fluxo--ages-2.0-alunos/dev/icones/spring.png"
        );

        assertEquals("https://signed/spring.png", result);
        verify(s3StorageService).createGetPresignedUrl("dev/icones/spring.png");
    }

    @Test
    @DisplayName("resolveForDisplay keeps external urls unchanged")
    void resolveForDisplayKeepsExternalUrlsUnchanged() {
        String result = storageReferenceResolver.resolveForDisplay("https://cdn.example.com/icon.svg");

        assertEquals("https://cdn.example.com/icon.svg", result);
        verify(s3StorageService, never()).createGetPresignedUrl("https://cdn.example.com/icon.svg");
    }

}
