package com.gestion.controller.product;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.product.ProductRequest;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.service.product.ProductCreatorService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductPostController {
    private final ProductCreatorService productCreatorService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(ProductRequest request,
                                                         @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        ProductResponse response = productCreatorService.createProduct(request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
