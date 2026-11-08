package com.gestion.controller.offer;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.offer.OfferProductIdsRequest;
import com.gestion.service.offer.OfferAssignerProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offers")
@AllArgsConstructor
public class OfferProductsPutController {

    private final OfferAssignerProductService offerAssignerProductService;

    @PutMapping("/{offerId}/products")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> assignOfferToProducts(@PathVariable Long offerId,
                                                      @Valid @RequestBody OfferProductIdsRequest request,
                                                      @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        offerAssignerProductService.assignOfferToProducts(offerId, request, authenticatedUser.getCommerceId());

        return ResponseEntity.ok().build();
    }
}
