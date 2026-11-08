package com.gestion.controller.offer;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.offer.OfferUpdateRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.service.offer.OfferUpdaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/offers")
@AllArgsConstructor
public class OfferPutController {
    private final OfferUpdaterService offerUpdaterService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OfferResponse> updateOffer(@PathVariable Long id,
                                                     @Valid @RequestBody OfferUpdateRequest request,
                                                     @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        OfferResponse response = offerUpdaterService.updateOffer(id, request, authenticatedUser.getCommerceId());
        return ResponseEntity.ok(response);
    }
}