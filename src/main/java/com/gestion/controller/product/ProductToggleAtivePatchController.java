package com.gestion.controller.product;

import com.gestion.config.UserPrincipal;
import com.gestion.service.product.ProductActiveUpdaterService;
import com.gestion.service.product.ProductUpdateActiveResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductToggleAtivePatchController {

    private ProductActiveUpdaterService productActiveUpdaterService;

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductUpdateActiveResponse> toggleProductActiveStatus(@PathVariable Long id,
                                                                                 @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        ProductUpdateActiveResponse response = productActiveUpdaterService.toggleActive(id, authenticatedUser.getCommerceId());
        return ResponseEntity.ok(response);
    }
}
