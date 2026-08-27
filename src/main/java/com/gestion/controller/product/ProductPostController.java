package com.gestion.controller.product;

import com.gestion.dto.request.product.ProductRequest;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.mappers.ProductMapper;
import com.gestion.model.Product;
import com.gestion.service.product.ProductCreatorService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductPostController {
    private final ProductCreatorService productCreatorService;
    private final ProductMapper productMapper;

    @PostMapping
    public ResponseEntity<ProductResponse> create(ProductRequest request, Long commerceId) {
        Product product = productCreatorService.createProduct(request, commerceId);
        ProductResponse response = productMapper.toResponse(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
