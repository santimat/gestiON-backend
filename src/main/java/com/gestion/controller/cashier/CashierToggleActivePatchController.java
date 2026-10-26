package com.gestion.controller.cashier;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.cashier.CashierUpdateActiveResponse;
import com.gestion.service.cashier.CashierActiveUpdaterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cashiers")
@AllArgsConstructor
public class CashierToggleActivePatchController {
    private final CashierActiveUpdaterService cashierActiveUpdaterService;

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<CashierUpdateActiveResponse> toggleCashierActiveStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        CashierUpdateActiveResponse response = cashierActiveUpdaterService
                .toggleActive(id, authenticatedUser.getCommerceId());
        return ResponseEntity.ok(response);
    }
}
