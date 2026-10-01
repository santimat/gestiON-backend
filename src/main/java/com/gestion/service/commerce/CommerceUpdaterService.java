package com.gestion.service.commerce;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import com.gestion.service.file.FileDeleterService;
import com.gestion.service.file.FileUploaderService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceUpdaterService {
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final JpaCommerceRepository commerceRepository;
    private final FileDeleterService fileDeleterService;
    private final FileUploaderService fileUploaderService;

    @Transactional
    public Commerce updateCommerce(CommerceRequest request, Long commerceId) {
        Commerce commerceToUpdate = commerceFinderByIdService.findById(commerceId);
        commerceToUpdate.setBusinessName(request.businessName());
        commerceToUpdate.setCuit(request.cuit());
        commerceToUpdate.setAddress(request.address());
        if (request.logo() != null && request.logo().getSize() > 0) {
            fileDeleterService.deleteFile(commerceToUpdate.getLogoName());
            String newLogoName = fileUploaderService.uploadFile(request.logo(), "business-logos");
            commerceToUpdate.setLogoName(newLogoName);
        }
        return commerceRepository.save(commerceToUpdate);
    }
}
