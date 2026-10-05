package com.gestion.service.offer;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.offer.OfferRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.mappers.OfferMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Offer;
import com.gestion.model.Product;
import com.gestion.repository.JpaOfferRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import com.gestion.service.product.ProductFinderByIdService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OfferCreatorService {
    private final JpaOfferRepository offerRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final ProductFinderByIdService productFinderByIdService;

    @Transactional
    public OfferResponse createOffer(OfferRequest request, UserPrincipal authenticatedUser) {
        Commerce commerce = commerceFinderByIdService.findCommerceById(authenticatedUser.getCommerceId());
        Product product = productFinderByIdService.findProductById(request.productId());
        LocalDateTime startDate = LocalDateTime.parse(request.startDate());
        LocalDateTime endDate = LocalDateTime.parse(request.endDate());
        Offer offerToSave = new Offer(null, product, commerce, request.value(), startDate, endDate);
        return OfferMapper.toResponse(offerRepository.save(offerToSave));
    }
}
