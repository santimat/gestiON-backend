package com.gestion.controller.offer;

import com.gestion.config.UserPrincipal;
import com.gestion.service.offer.OfferProductIdsFinderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@AllArgsConstructor
public class OfferProductIdsGetController {

    private final OfferProductIdsFinderService offerProductIdsFinderService;

    @GetMapping("/{offerId}/product-ids")
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<List<Long>> getOfferProductIds(
            @PathVariable Long offerId,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(
                offerProductIdsFinderService.findProductIdsByOfferIdAndCommerceId(
                        offerId, authenticatedUser.getCommerceId()));
    }
}
