package com.gestion.service.commerce;

import com.gestion.dto.response.commerce.CurrentCommerceResponse;
import com.gestion.mappers.CommerceMapper;
import com.gestion.model.Commerce;
import com.gestion.properties.MinioProperties;
import com.gestion.service.file.FileFinderService;
import io.minio.errors.MinioException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceCurrentFinderService {
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final FileFinderService fileFinderService;
    private final MinioProperties minioDirProperties;

    public CurrentCommerceResponse findCurrentCommerce(Long commerceId) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);

        String businessLogoUrl = null;

        if (commerce.getLogoName() != null) {
            try {
                businessLogoUrl = fileFinderService.getObjectUrl(commerce.getLogoName(),
                        minioDirProperties.dir().businessLogos());
            } catch (MinioException e) {
                System.out.println("Error retrieving business logo from MinIO: " + e.getCause());
            }
        }

        return CommerceMapper.toCurrentResponse(commerce, businessLogoUrl);
    }
}
