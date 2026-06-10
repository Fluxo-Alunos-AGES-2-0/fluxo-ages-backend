package com.fluxo.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private final S3 s3 = new S3();

    @Getter
    @Setter
    public static class S3 {
        private String bucket;
        private String region;
        private String keyPrefix = "";
        private Integer presignedUrlDurationMinutes = 60;
        private String accessKey;
        private String secretKey;
    }
}
