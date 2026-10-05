package com.gestion.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        String url,
        String accessKey,
        String secretKey,
        String bucket,
        Dir dir
) {
    public record Dir(
            String businessLogos,
            String productImages
    ) {
    }
}
