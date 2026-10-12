package com.gestion.service.file;

import com.gestion.properties.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FileFinderService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public FileFinderService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public String getObjectUrl(String objectName, String subDir) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(minioProperties.bucket())
                            .object(subDir + "/" + objectName)
                            .expiry(5, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            System.out.println("Error retrieving object URL from MinIO: " + e.getCause());
            return null;
        }
    }
}
