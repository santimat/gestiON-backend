package com.gestion.controller.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sale.SaleResponse;
import com.gestion.service.sale.SaleWithDetailsFinderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@AllArgsConstructor
public class SaleWithDetailsGetController {
    private final SaleWithDetailsFinderService saleWithDetailsFinderService;

    @GetMapping("/{saleId}")
    public ResponseEntity<SaleResponse> getSaleWithDetails(@PathVariable Long saleId,
                                                           @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        SaleResponse saleResponse = saleWithDetailsFinderService.findBySaleIdAndCommerceId(saleId, authenticatedUser.getCommerceId());
        return ResponseEntity.ok(saleResponse);
    }
}
