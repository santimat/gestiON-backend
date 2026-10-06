package com.gestion.service.file;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
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

    // TODO: preguntar como evitar que si el bucket está sin conexión, los recursos se muestren igual, pero sin las
    //  imagenes
    public String getObjectUrl(String objectName, String subDir) throws MinioException {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Http.Method.GET)
                        .bucket(bucketName)
                        .object(subDir + "/" + objectName)
                        .expiry(5, TimeUnit.HOURS)
                        .build()
        );
    }
}
