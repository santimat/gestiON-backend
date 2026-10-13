package com.gestion.service.tika;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.FileException;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

public class MimeTypeDetecterService {
    private final static Tika tika = new Tika();

    public static String detectMimeType(MultipartFile file) {
        try {
            return tika.detect(file.getBytes());
        } catch (Exception e) {
            throw new FileException(ErrorCode.FILE_PROCESSING_FAILED, "Error while detecting file mime type", e);
        }
    }
}
