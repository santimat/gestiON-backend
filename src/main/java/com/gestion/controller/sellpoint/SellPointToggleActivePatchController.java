package com.gestion.controller.sellpoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sellpoint.SellPointUpdateActiveResponse;
import com.gestion.service.sellpoint.SellPointActiveUpdaterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sell-points")
@AllArgsConstructor
public class SellPointToggleActivePatchController {
    private final SellPointActiveUpdaterService sellPointActiveUpdaterService;

    @PatchMapping("/{sellPointId}/toggle-active")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SellPointUpdateActiveResponse> toggleSellPointActiveStatus(
            @PathVariable Long sellPointId,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        SellPointUpdateActiveResponse response = sellPointActiveUpdaterService
                .toggleActive(sellPointId, authenticatedUser.getCommerceId());
        return ResponseEntity.ok(response);
    }
}
