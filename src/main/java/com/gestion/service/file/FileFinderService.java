package com.gestion.service.file;

import com.gestion.exception.FileException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class FileFinderService {
    private final MinioClient minioClient;
    @Value("${minio.bucket}")
    private String bucketName;

    public FileFinderService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String getObjectUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(5, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            throw new FileException("An error occurred when getting file url: " + e.getMessage());
        }
    }
}
