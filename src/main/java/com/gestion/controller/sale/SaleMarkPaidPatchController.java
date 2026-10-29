package com.gestion.controller.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sale.SaleUpdateStatusResponse;
import com.gestion.service.sale.SaleMarkPaidService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@AllArgsConstructor
public class SaleMarkPaidPatchController {
    private final SaleMarkPaidService saleMarkPaidService;

    @PatchMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('CASHIER') or hasRole('OWNER')")
    public ResponseEntity<SaleUpdateStatusResponse> markPaid(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(saleMarkPaidService.markPaid(id, authenticatedUser));
    }
}
