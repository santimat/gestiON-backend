package com.gestion.controller.offer;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.service.offer.OfferSearcherService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<OfferResponse>> getAllOffers(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam Long commerceId) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortDir);
        Sort sortConfig = Sort.by(sortDirection, sortBy);
        Pageable pageable = PageRequest.of(page, size, sortConfig);

        return ResponseEntity.ok(offerSearcherService.searchOffers(pageable, commerceId));
    }
}
