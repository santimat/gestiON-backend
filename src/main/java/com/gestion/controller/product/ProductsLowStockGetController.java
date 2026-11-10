package com.gestion.controller.product;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.product.ProductLowStockResponse;
import com.gestion.service.product.ProductLowStockFinderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductsLowStockGetController {

    private final ProductLowStockFinderService productLowStockFinderService;

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ProductLowStockResponse>> getLowStockProducts(
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        return ResponseEntity.ok(productLowStockFinderService
                .findLowStockProducts(authenticatedUser.getCommerceId()));
    }
}