package com.gestion.service.offer;

import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class OfferProductIdsFinderService {

    private final OfferFinderByIdAndCommerceIdService offerFinderByIdAndCommerceIdService;
    private final JpaProductRepository jpaProductRepository;

    @Transactional(readOnly = true)
    public List<Long> findProductIdsByOfferIdAndCommerceId(Long offerId, Long commerceId) {
        offerFinderByIdAndCommerceIdService.findOfferByIdAndCommerceId(offerId, commerceId);
        return jpaProductRepository.findIdsByOfferIdAndCommerceId(offerId, commerceId);
    }
}
