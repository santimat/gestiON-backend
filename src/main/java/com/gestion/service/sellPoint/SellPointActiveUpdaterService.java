package com.gestion.service.sellPoint;

import com.gestion.dto.response.sellPoint.SellPointUpdateActiveResponse;
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
        var sellPointToUpdate = sellPointFinderByIdAndCommerceIdService
                .findSellPointByIdAndCommerceId(sellPointId, commerceId);
        sellPointToUpdate.setActive(!Boolean.TRUE.equals(sellPointToUpdate.getActive()));
        var updatedSellPoint = sellPointRepository.save(sellPointToUpdate);
        return new SellPointUpdateActiveResponse(
                updatedSellPoint.getId(),
                updatedSellPoint.getActive()
        );
    }
}
