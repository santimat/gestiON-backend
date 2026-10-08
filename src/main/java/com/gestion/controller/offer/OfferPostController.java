package com.gestion.controller.offer;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.offer.OfferRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.service.offer.OfferCreatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/offers")
@AllArgsConstructor
public class OfferPostController {
    private final OfferCreatorService offerCreatorService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OfferResponse> createOffer(@RequestBody @Valid OfferRequest request,
                                                     @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        OfferResponse response = offerCreatorService.createOffer(request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
