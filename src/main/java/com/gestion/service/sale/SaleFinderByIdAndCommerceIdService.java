package com.gestion.service.sale;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Sale;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SaleFinderByIdAndCommerceIdService {
    private final JpaSaleRepository jpaSaleRepository;

    @Transactional(readOnly = true)
    public Sale findBy(Long id, Long commerceId) {
        return jpaSaleRepository.findByIdAndCommerceId(id, commerceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SALE_NOT_FOUND,
                        "Sale with id " + id + " not found in this commerce"));
    }
}
