package com.gestion.service.commerce;

import com.gestion.dto.response.commerce.CommerceProfitMultiplierResponse;
import com.gestion.mappers.CommerceProfitMultiplierMapper;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceProfitMultiplierUpdaterService {
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final JpaCommerceRepository commerceRepository;

    @Transactional
    public CommerceProfitMultiplierResponse updateProfitMultiplier(Double profitMultiplier, Long commerceId) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);
        commerce.setProfitMultiplier(profitMultiplier);
        Commerce updatedCommerce = commerceRepository.save(commerce);
        return CommerceProfitMultiplierMapper.toResponse(updatedCommerce);
    }
}
