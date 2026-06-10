package com.fluxo.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class StorageConfig {

    @Bean
    public S3Client s3Client(StorageProperties storageProperties) {
        StorageProperties.S3 properties = storageProperties.getS3();
        validateS3Configuration(properties);

        var builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .serviceConfiguration(S3Configuration.builder()
                        .checksumValidationEnabled(false)
                        .build());

        if (hasStaticCredentials(properties)) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
            ));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(StorageProperties storageProperties) {
        StorageProperties.S3 properties = storageProperties.getS3();
        validateS3Configuration(properties);

        var builder = S3Presigner.builder()
                .region(Region.of(properties.getRegion()));

        if (hasStaticCredentials(properties)) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
            ));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }

    private boolean hasStaticCredentials(StorageProperties.S3 properties) {
        return StringUtils.hasText(properties.getAccessKey())
                && StringUtils.hasText(properties.getSecretKey());
    }

    private void validateS3Configuration(StorageProperties.S3 properties) {
        if (!StringUtils.hasText(properties.getRegion())) {
            throw new IllegalStateException("A regiao do S3 deve ser configurada em app.storage.s3.region.");
        }

        if (!StringUtils.hasText(properties.getBucket())) {
            throw new IllegalStateException("O bucket do S3 deve ser configurado em app.storage.s3.bucket.");
        }
    }
}
