package com.gestion.service.sellpoint;

import com.gestion.dto.response.sellpoint.SellPointUpdateActiveResponse;
import com.gestion.model.SellPoint;
import com.gestion.repository.JpaSellPointRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SellPointActiveUpdaterService {
    private final SellPointFinderByIdAndCommerceIdService sellPointFinderByIdAndCommerceIdService;
    private final JpaSellPointRepository sellPointRepository;

    @Transactional
    public SellPointUpdateActiveResponse toggleActive(Long sellPointId, Long commerceId) {
        SellPoint sellPointToUpdate = sellPointFinderByIdAndCommerceIdService
                .findSellPointByIdAndCommerceId(sellPointId, commerceId);
        sellPointToUpdate.setActive(!sellPointToUpdate.isActive());
        SellPoint updatedSellPoint = sellPointRepository.save(sellPointToUpdate);
        return new SellPointUpdateActiveResponse(
                updatedSellPoint.getId(),
                updatedSellPoint.isActive()
        );
    }
}
