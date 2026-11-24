package com.gestion.service.offer;

import com.gestion.dto.project.offer.OfferProductCount;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OfferProductCounterService {
    private final JpaProductRepository jpaProductRepository;

    public Map<Long, Long> countProductsByOfferIds(Long commerceId, List<Long> offerIds) {
        if (offerIds.isEmpty()) {
            return Map.of();
        }

        return jpaProductRepository.countByOfferIds(commerceId, offerIds).stream()
                .collect(Collectors.toMap(
                        OfferProductCount::offerId,
                        OfferProductCount::productCount));
    }
}
