package com.gestion.service.file;

import com.gestion.dto.request.file.FileRequest;
import com.gestion.exception.FileException;
import com.gestion.service.tika.MimeTypeValidatorService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;


@Service
public class FileUploaderService {

    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    public FileUploaderService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String uploadFile(MultipartFile file) {
        // 1. Validar tipo de archivo
        if (!MimeTypeValidatorService.isValidMimeType(file))
            throw new FileException("File's MimeType is invalid");

        // 2. Crear nombre único para el archivo
        String objectName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();

        try{
            // 3. Subir el archivo
            minioClient.putObject(
                    // constructor para los argumentos
                    PutObjectArgs.builder()
                            // en que bucket queremos guardarlo
                            .bucket(bucketName)
                            // el nombre que tendrá
                            .object(objectName)
                            // le pasamos los bytes en forma de flujo de datos
                            .stream(file.getInputStream(), file.getSize(), -1L)
                            .contentType(file.getContentType())
                            .build()
            );
        }catch(Exception e){
            throw new FileException("An error occurred while saving the file" + e.getMessage());
        }

        return objectName;
    }
}
