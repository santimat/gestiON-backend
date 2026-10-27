package com.gestion.service.sellpoint;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.SellPoint;
import com.gestion.repository.JpaSellPointRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SellPointFinderByIdAndCommerceIdService {
    private final JpaSellPointRepository sellPointRepository;

    @Transactional(readOnly = true)
    public SellPoint findSellPointByIdAndCommerceId(Long id, Long commerceId) {
        return sellPointRepository.findByIdAndCommerceId(id, commerceId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELL_POINT_NOT_FOUND,
                        "Sell point with id " + id + " not found in this commerce"));
    }
}
