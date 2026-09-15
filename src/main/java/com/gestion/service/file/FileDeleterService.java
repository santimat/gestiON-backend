package com.gestion.service.file;

import com.gestion.exception.FileException;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileDeleterService {

    private final MinioClient minioClient;
    @Value("${minio.bucket}")
    private String bucketName;

    public FileDeleterService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new FileException("An error occurred when deleting file" + e.getMessage());
        }
    }
}
