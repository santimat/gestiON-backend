package com.gestion.service.file;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.FileException;
import com.gestion.properties.MinioProperties;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import org.springframework.stereotype.Service;

@Service
public class FileDeleterService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public FileDeleterService(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.bucket())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new FileException(ErrorCode.FILE_DELETE_FAILED, "An error occurred while deleting the file", e);
        }
    }
}
