package com.gestion.controller.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.sale.SalePaymentRequest;
import com.gestion.dto.response.transaction.TransactionResponse;
import com.gestion.service.sale.SalePaymentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@AllArgsConstructor
public class SalePaymentPostController {

    private final SalePaymentService salePaymentService;

    @PostMapping("/{saleId}/payments")
    @PreAuthorize("hasRole('OWNER') or hasRole('CASHIER')")
    public ResponseEntity<TransactionResponse> registerPayment(
            @AuthenticationPrincipal UserPrincipal authenticatedUser,
            @org.springframework.web.bind.annotation.PathVariable Long saleId,
            @RequestBody SalePaymentRequest request) {
        TransactionResponse response = salePaymentService.registerPayment(
                saleId, authenticatedUser.getCommerceId(), request.amount());
        return ResponseEntity.ok(response);
    }
}