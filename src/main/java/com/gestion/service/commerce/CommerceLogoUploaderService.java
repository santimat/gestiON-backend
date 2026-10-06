package com.gestion.service.commerce;

import com.gestion.exception.FileException;
import com.gestion.model.Commerce;
import com.gestion.properties.MinioProperties;
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
    private final MinioProperties minioDirProperties;

    public String uploadLogoAndGetName(MultipartFile logo, Commerce commerce) {
        String bussinesLogoName = fileUploaderService.uploadFile(logo, minioDirProperties.dir().businessLogos());

        commerce.setLogoName(bussinesLogoName);
        commerceRepository.save(commerce);

        try {
            return fileFinderService.getObjectUrl(bussinesLogoName, minioDirProperties.dir().businessLogos());
        } catch (Exception e) {
            throw new FileException("", e.getCause());
        }
    }
}
