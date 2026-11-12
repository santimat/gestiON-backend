package com.gestion.controller.offer;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.enums.OfferStatus;
import com.gestion.enums.OfferType;
import com.gestion.enums.PaymentMethod;
import com.gestion.service.offer.OfferSearcherService;
import com.gestion.utils.PageableFactory;
import com.gestion.utils.SortableProperties;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offers")
@AllArgsConstructor
public class OffersGetController {
    private final OfferSearcherService offerSearcherService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<Page<OfferResponse>> searchOffers(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestParam(required = false) OfferStatus status,
            @RequestParam(required = false) PaymentMethod paymentMethod,
            @RequestParam(required = false) OfferType offerType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "endDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Pageable pageable = PageableFactory.create(page, size, sortBy, sortDir, SortableProperties.OFFER);
        return ResponseEntity.ok(offerSearcherService
                .searchOffers(authenticatedUser.getCommerceId(), status, paymentMethod, offerType, pageable));
    }
}
