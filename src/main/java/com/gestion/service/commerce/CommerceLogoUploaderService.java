package com.gestion.service.commerce;

import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import com.gestion.service.file.FileFinderService;
import com.gestion.service.file.FileUploaderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class CommerceLogoUploaderService {
    private final JpaCommerceRepository commerceRepository;
    private final FileUploaderService fileUploaderService;
    private final FileFinderService fileFinderService;

    public String uploadLogoAndGetName(MultipartFile logo, Commerce commerce) {
        String bussinesLogoName = fileUploaderService.uploadFile(logo, "business-logos");

        commerce.setLogoName(bussinesLogoName);
        commerceRepository.save(commerce);

        return fileFinderService.getObjectUrl(bussinesLogoName, "business-logos");
    }
}
