package com.gestion.service.offer;

import com.gestion.dto.request.offer.OfferRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.mappers.OfferMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Offer;
import com.gestion.repository.JpaOfferRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class OfferCreatorService {
    private final JpaOfferRepository offerRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;

    @Transactional
    public OfferResponse createOffer(OfferRequest request, Long commerceId) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);
        LocalDateTime startDate = LocalDateTime.parse(request.startDate());
        LocalDateTime endDate = LocalDateTime.parse(request.endDate());
        Offer offerToSave = new Offer(null, commerce, request.value(), startDate, endDate, request.paymentMethods());
        return OfferMapper.toResponse(offerRepository.save(offerToSave));
    }
}
