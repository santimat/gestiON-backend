package com.gestion.dto.request.file;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record FileRequest(
        @NotNull
        MultipartFile file
) {
}
