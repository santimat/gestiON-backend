package com.gestion.controller.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.response.sale.SaleResponse;
import com.gestion.service.sale.SaleCreatorService;
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
@RequestMapping("/api/sales")
@AllArgsConstructor
public class SalesPostController {
    private final SaleCreatorService saleCreatorService;

    @PostMapping
    @PreAuthorize("hasRole('CASHIER') or hasRole('OWNER')")
    public ResponseEntity<SaleResponse> createSale(@RequestBody @Valid SaleRequest request,
                                                   @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        SaleResponse response = saleCreatorService.createSale(request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
