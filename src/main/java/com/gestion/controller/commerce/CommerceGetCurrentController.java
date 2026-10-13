package com.gestion.controller.commerce;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.commerce.CurrentCommerceResponse;
import com.gestion.service.commerce.CommerceCurrentFinderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commerces")
@AllArgsConstructor
public class CommerceGetCurrentController {
    private final CommerceCurrentFinderService commerceCurrentFinderService;

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('OWNER', 'CASHIER')")
    public ResponseEntity<CurrentCommerceResponse> getCurrentCommerce(
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(commerceCurrentFinderService.findCurrentCommerce(authenticatedUser.getCommerceId()));
    }
}
