package com.gestion.controller.sellPoint;

import com.gestion.config.UserPrincipal;
import com.gestion.service.sellPoint.SellPointCashierRemoverService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sellpoints")
@AllArgsConstructor
public class SellPointCashierDeleteController {
    private final SellPointCashierRemoverService sellPointCashierRemoverService;

    @DeleteMapping("/{id}/cashiers/{cashierId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> removeCashier(@PathVariable Long id,
                                              @PathVariable Long cashierId,
                                              @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        sellPointCashierRemoverService.removeCashier(id, cashierId, authenticatedUser);
        return ResponseEntity.noContent().build();
    }
}
