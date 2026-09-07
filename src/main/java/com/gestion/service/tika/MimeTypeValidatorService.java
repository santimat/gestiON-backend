package com.gestion.service.tika;


import com.gestion.exception.FileManagerException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Service
public class MimeTypeValidatorService {

    private final static Tika tika = new Tika();

    private final static Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpg",
            "image/png",
            "image/jpeg",
            "image/webp"
    );

    public static boolean isValidMimeType(MultipartFile file) {
        try{
            String fileMimeType = tika.detect(file.getInputStream());
            return ALLOWED_MIME_TYPES.contains(fileMimeType);
        }catch(IOException e){
            throw new FileManagerException("Error while validating file mime type: " + e.getMessage());
        }

    }
}
