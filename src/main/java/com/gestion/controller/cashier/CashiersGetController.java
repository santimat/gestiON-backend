package com.gestion.controller.cashier;

import com.gestion.dto.response.user.UserResponse;
import com.gestion.service.cashier.CashierSearcherService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cashiers")
@AllArgsConstructor
public class CashiersGetController {
    private final CashierSearcherService cashierSearcherService;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Page<UserResponse>> getAllCashiers(Pageable pageable) {
        Page<UserResponse> cashiers = cashierSearcherService.searchCashier(pageable);
        return ResponseEntity.ok(cashiers);
    }
}
