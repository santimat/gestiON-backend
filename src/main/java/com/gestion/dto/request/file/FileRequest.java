package com.gestion.dto.request.file;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record FileRequest(
        @NotNull
        MultipartFile file,
        @NotBlank
        @Length(min=5, max = 100,message = "The sub directory name must be between 5 and 100 characters")
        String subDirName
) {
}
