package com.gestion.controller.file;

import com.gestion.dto.request.file.FileRequest;
import com.gestion.service.file.FileUploaderService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@AllArgsConstructor
public class FilePostController {
    private final FileUploaderService fileUploaderService;

    @PostMapping
    public void uploadFile(@RequestBody @Valid FileRequest fileRequest){
        fileUploaderService.uploadFile(fileRequest);
    }
}
