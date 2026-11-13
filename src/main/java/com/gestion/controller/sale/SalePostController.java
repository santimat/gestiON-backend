package com.gestion.controller.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.response.sale.SaleSummaryResponse;
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
public class SalePostController {
    private final SaleCreatorService saleCreatorService;

    @PostMapping
    @PreAuthorize("hasRole('CASHIER') or hasRole('OWNER')")
    public ResponseEntity<SaleSummaryResponse> createSale(@RequestBody @Valid SaleRequest request,
                                                          @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        SaleSummaryResponse response = saleCreatorService.createSale(request, authenticatedUser.getId(), authenticatedUser.getCommerceId(), authenticatedUser.getSellPointId(), authenticatedUser.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
