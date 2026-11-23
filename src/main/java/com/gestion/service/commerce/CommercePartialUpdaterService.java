package com.gestion.service.commerce;

import com.gestion.dto.request.commerce.CommercePatchRequest;
import com.gestion.dto.response.commerce.CurrentCommerceResponse;
import com.gestion.mappers.CommerceMapper;
import com.gestion.model.Commerce;
import com.gestion.properties.MinioProperties;
import com.gestion.repository.JpaCommerceRepository;
import com.gestion.service.file.FileDeleterService;
import com.gestion.service.file.FileFinderService;
import com.gestion.service.file.FileUploaderService;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommercePartialUpdaterService {

    private final CommerceFinderByIdService commerceFinderByIdService;
    private final JpaCommerceRepository commerceRepository;
    private final FileDeleterService fileDeleterService;
    private final FileUploaderService fileUploaderService;
    private final FileFinderService fileFinderService;
    private final MinioProperties minioProperties;

    @Transactional
    public CurrentCommerceResponse partialUpdate(CommercePatchRequest request, Long commerceId) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);

        if (request.name() != null) {
            commerce.setName(request.name());
        }

        if (request.profitMultiplier() != null) {
            commerce.setProfitMultiplier(request.profitMultiplier());
        }

        if (request.image() != null) {
            if (commerce.getLogoName() != null) {
                fileDeleterService.deleteFile(commerce.getLogoName(), minioProperties.dir().businessLogos());
            }
            String newLogoName = fileUploaderService.uploadFile(request.image(), minioProperties.dir().businessLogos());
            commerce.setLogoName(newLogoName);
        }

        Commerce updatedCommerce = commerceRepository.save(commerce);

        String businessLogoUrl = fileFinderService.getObjectUrl(
                updatedCommerce.getLogoName(), minioProperties.dir().businessLogos());

        return CommerceMapper.toCurrentResponse(updatedCommerce, businessLogoUrl);
    }

}