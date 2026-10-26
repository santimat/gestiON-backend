package com.gestion.controller.cashier;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.cashier.CashierUpdateRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.service.cashier.CashierUpdaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cashiers")
@AllArgsConstructor
public class CashierPutController {
    private final CashierUpdaterService cashierUpdaterService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<UserResponse> updateCashier(@PathVariable Long id,
                                                      @RequestBody @Valid CashierUpdateRequest request,
                                                      @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        UserResponse response = cashierUpdaterService.updateCashier(id, request, authenticatedUser);
        return ResponseEntity.ok(response);
    }
}
