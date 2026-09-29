package com.gestion.service.file;

import com.gestion.exception.FileException;
import com.gestion.service.tika.MimeTypeDetecterService;
import com.gestion.service.tika.MimeTypeValidatorService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@Service
public class FileUploaderService {

    private final MinioClient minioClient;
    @Value("${minio.bucket}")
    private String bucketName;

    public FileUploaderService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadFile(MultipartFile file, String subDir) {
        // 1. Validar tipo de archivo
        if (!MimeTypeValidatorService.isValidMimeType(file))
            throw new FileException("File's MimeType is invalid");

        // 2. validar tamaño del archivo
        if (file.getSize() > 10 * 1024 * 1024) // 10 MB
            throw new FileException("File size exceeds the maximum limit of 10 MB");

        // 3. Crear nombre único para el archivo
        String objectName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        // 4. Detectar el tipo de archivo
        String fileMimeType = MimeTypeDetecterService.detectMimeType(file);

        try {
            // 5. Subir el archivo
            minioClient.putObject(
                    // constructor para los argumentos
                    PutObjectArgs.builder()
                            // en que bucket queremos guardarlo
                            .bucket(bucketName)
                            // el nombre que tendrá
                            .object(subDir + "/" + objectName)
                            // le pasamos los bytes en forma de flujo de datos
                            .stream(file.getInputStream(), file.getSize(), -1L)
                            .contentType(fileMimeType)
                            .build()
            );
        } catch (Exception e) {
            throw new FileException("An error occurred while saving the file" + e.getMessage());
        }

        return objectName;
    }
}
