package com.gestion.controller.product;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.product.ProductUpdateRequest;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.service.product.ProductUpdaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductPutController {
    private final ProductUpdaterService productUpdaterService;

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @ModelAttribute ProductUpdateRequest request,
                                                         @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        ProductResponse response = productUpdaterService.updateProduct(id, request, authenticatedUser);
        return ResponseEntity.ok(response);
    }
}
