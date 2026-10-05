package com.gestion.service.commerce;

import com.gestion.dto.response.commerce.CommerceUpdateActiveResponse;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceUpdaterActiveService {
    private final JpaCommerceRepository commerceRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;

    public CommerceUpdateActiveResponse toggleActive(Long commerceId) {
        Commerce commerceToUpdate = commerceFinderByIdService.findById(commerceId);
        commerceToUpdate.setActive(!commerceToUpdate.isActive());
        Commerce updatedCommerce = commerceRepository.save(commerceToUpdate);
        return new CommerceUpdateActiveResponse(
                updatedCommerce.getId(),
                updatedCommerce.isActive()
        );
    }
}
