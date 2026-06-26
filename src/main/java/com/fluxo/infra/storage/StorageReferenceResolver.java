package com.fluxo.infra.storage;

import com.fluxo.infra.config.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class StorageReferenceResolver {

    private static final String S3_REFERENCE_PREFIX = "s3:";
    private static final String S3_URI_PREFIX = "s3://";

    private final S3StorageService s3StorageService;
    private final StorageProperties storageProperties;

    public String resolveForDisplay(String reference) {
        if (!StringUtils.hasText(reference)) {
            return reference;
        }

        return extractManagedS3Key(reference)
                .map(s3StorageService::createGetPresignedUrl)
                .orElse(reference);
    }

    private Optional<String> extractManagedS3Key(String reference) {
        if (reference.startsWith(S3_REFERENCE_PREFIX) && !reference.startsWith(S3_URI_PREFIX)) {
            return Optional.of(s3StorageService.normalizePath(reference.substring(S3_REFERENCE_PREFIX.length())));
        }

        if (reference.startsWith(S3_URI_PREFIX)) {
            return extractKeyFromS3Uri(reference);
        }

        if (looksLikeAbsoluteUrl(reference)) {
            return extractKeyFromHttpUrl(reference);
        }

        return Optional.of(s3StorageService.normalizePath(reference));
    }

    private Optional<String> extractKeyFromS3Uri(String reference) {
        try {
            URI uri = URI.create(reference);
            String configuredBucket = normalizeBucket(storageProperties.getS3().getBucket());
            String bucket = normalizeBucket(StringUtils.hasText(uri.getHost()) ? uri.getHost() : uri.getAuthority());
            String key = s3StorageService.normalizePath(uri.getPath());

            if (!StringUtils.hasText(bucket) || !StringUtils.hasText(key)) {
                return Optional.empty();
            }

            if (StringUtils.hasText(configuredBucket) && !configuredBucket.equals(bucket)) {
                return Optional.empty();
            }

            return Optional.of(key);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<String> extractKeyFromHttpUrl(String reference) {
        try {
            URI uri = URI.create(reference);
            String host = normalizeBucket(uri.getHost());
            String configuredBucket = normalizeBucket(storageProperties.getS3().getBucket());
            String normalizedPath = s3StorageService.normalizePath(uri.getPath());

            if (!StringUtils.hasText(host) || !host.endsWith(".amazonaws.com")) {
                return Optional.empty();
            }

            if (host.equals(configuredBucket + ".s3.amazonaws.com")
                    || host.startsWith(configuredBucket + ".s3.")) {
                return StringUtils.hasText(normalizedPath)
                        ? Optional.of(normalizedPath)
                        : Optional.empty();
            }

            if (host.equals("s3.amazonaws.com") || host.startsWith("s3.")) {
                return extractKeyFromPathStyleUrl(normalizedPath, configuredBucket);
            }

            return Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<String> extractKeyFromPathStyleUrl(String normalizedPath, String configuredBucket) {
        if (!StringUtils.hasText(normalizedPath)) {
            return Optional.empty();
        }

        String[] segments = normalizedPath.split("/", 2);
        if (segments.length < 2) {
            return Optional.empty();
        }

        String bucket = normalizeBucket(segments[0]);
        if (!bucket.equals(configuredBucket)) {
            return Optional.empty();
        }

        String key = s3StorageService.normalizePath(segments[1]);
        return StringUtils.hasText(key) ? Optional.of(key) : Optional.empty();
    }

    private boolean looksLikeAbsoluteUrl(String value) {
        try {
            return URI.create(value).isAbsolute();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String normalizeBucket(String bucket) {
        return bucket == null ? "" : bucket.trim().toLowerCase(Locale.ROOT);
    }
}
