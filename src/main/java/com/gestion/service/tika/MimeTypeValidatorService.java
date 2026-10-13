package com.gestion.service.tika;


import com.gestion.enums.ErrorCode;
import com.gestion.exception.FileException;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

public class MimeTypeValidatorService {

    private final static Tika tika = new Tika();

    private final static Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpg",
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    public static boolean isValidMimeType(MultipartFile file) {
        try {
            String fileMimeType = tika.detect(file.getInputStream());
            return ALLOWED_MIME_TYPES.contains(fileMimeType);
        } catch (IOException e) {
            throw new FileException(ErrorCode.FILE_PROCESSING_FAILED, "Error while validating file mime type", e);
        }
    }
}
