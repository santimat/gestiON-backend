package com.gestion.dto.request.file;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record FileRequest(
        @NotNull
        MultipartFile file
) {
}
