package com.gestion.service.file;

import com.gestion.properties.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class FileFinderService {

    private static final int EXPIRY_HOURS = 1;

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public String getObjectUrl(String objectName, String subDir) {
        String objectKey = FileObjectKey.of(subDir, objectName);
        if (objectKey == null) {
            return null;
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(minioProperties.bucket())
                            .object(objectKey)
                            .expiry(EXPIRY_HOURS, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error retrieving object URL from MinIO: {}", e.getMessage(), e);
            return null;
        }
    }
}
