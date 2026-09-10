package com.gestion.controller.file;

import com.gestion.service.file.FileUploaderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
public class FilePostController {
    private final FileUploaderService fileUploaderService;

    @PostMapping
    public ResponseEntity<String> uploadFile(MultipartFile file){
        String objectName = fileUploaderService.uploadFile(file);

        return ResponseEntity.ok(objectName);
    }
}
