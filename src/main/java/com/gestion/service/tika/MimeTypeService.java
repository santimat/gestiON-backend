package com.gestion.service.tika;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.FileException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;

@Service
public class MimeTypeService {

    // Tika detecta por contenido: un .jpg real se reporta como "image/jpeg", nunca como
    // "image/jpg", asi que ese valor del allow-list era inalcanzable.
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    private final Tika tika = new Tika();

    public String detect(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return tika.detect(inputStream);
        } catch (Exception e) {
            throw new FileException(ErrorCode.FILE_PROCESSING_FAILED, "Error while detecting file mime type", e);
        }
    }

    public boolean isAllowed(String mimeType) {
        return ALLOWED_MIME_TYPES.contains(mimeType);
    }
}
