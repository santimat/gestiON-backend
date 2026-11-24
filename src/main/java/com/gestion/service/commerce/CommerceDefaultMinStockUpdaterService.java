package com.gestion.service.commerce;

import com.gestion.dto.response.commerce.CommerceDefaultMinStockResponse;
import com.gestion.mappers.CommerceDefaultMinStockMapper;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CommerceDefaultMinStockUpdaterService {
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final JpaCommerceRepository commerceRepository;

    @Transactional
    public CommerceDefaultMinStockResponse updateDefaultMinStock(Integer defaultMinStock, Long commerceId) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);
        commerce.setDefaultMinStock(defaultMinStock);
        Commerce updatedCommerce = commerceRepository.save(commerce);
        return CommerceDefaultMinStockMapper.toResponse(updatedCommerce);
    }
}
