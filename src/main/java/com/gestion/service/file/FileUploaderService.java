package com.gestion.service.file;

import com.gestion.dto.request.file.FileRequest;
import com.gestion.exception.FileException;
import com.gestion.service.tika.MimeTypeValidatorService;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class FileUploaderService {
    private final MinioClient minioClient;

    public void uploadFile(FileRequest request) {
        // 1. Validar tipo de archivo
        if (MimeTypeValidatorService.isValidMimeType(request.file()))
            throw new FileException("File's MimeType is invalid");
        

    }
}
