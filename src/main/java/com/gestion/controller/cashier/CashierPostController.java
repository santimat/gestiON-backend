package com.gestion.controller.cashier;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.cashier.CashierRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.service.cashier.CashierCreatorService;
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
@RequestMapping("/api/cashiers")
@AllArgsConstructor
public class CashierPostController {
    private final CashierCreatorService cashierCreatorService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<UserResponse> createCashier(@RequestBody @Valid CashierRequest request,
                                                      @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        UserResponse response = cashierCreatorService.createCashier(request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
