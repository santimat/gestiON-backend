package com.gestion.controller.sellPoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sellPoint.SellPointUpdateActiveResponse;
import com.gestion.service.sellPoint.SellPointActiveUpdaterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellpoints")
@AllArgsConstructor
public class SellPointToggleActivePatchController {
    private final SellPointActiveUpdaterService sellPointActiveUpdaterService;

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<SellPointUpdateActiveResponse> toggleSellPointActiveStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        SellPointUpdateActiveResponse response = sellPointActiveUpdaterService
                .toggleActive(id, authenticatedUser.getCommerceId());
        return ResponseEntity.ok(response);
    }
}
