package com.gestion.service.file;

import com.gestion.dto.request.file.FileRequest;
import com.gestion.service.tika.MimeTypeValidatorService;
import io.minio.MinioClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class FileUploaderService {
    private final MinioClient minioClient;
    private final MimeTypeValidatorService mimeTypeValidatorService;
    public void uploadFile(FileRequest request){
        // 1. Validar tipo de archivo

    }
}
