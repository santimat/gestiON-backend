package com.gestion.controller.offer;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.offer.OfferPaymentMethodResponse;
import com.gestion.service.offer.OfferPaymentMethodsFinderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@AllArgsConstructor
public class OfferPaymentMethodsGetController {

    private final OfferPaymentMethodsFinderService offerPaymentMethodsFinderService;

    @GetMapping("/payment-methods")
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<List<OfferPaymentMethodResponse>> getActivePaymentMethodOffers(
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(
                offerPaymentMethodsFinderService.findActivePaymentMethodOffers(authenticatedUser.getCommerceId()));
    }
}