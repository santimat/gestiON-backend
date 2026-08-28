package com.gestion.controller.product;

import com.gestion.dto.response.product.ProductResponse;
import com.gestion.mappers.ProductMapper;
import com.gestion.model.Product;
import com.gestion.service.product.ProductFinderByIdService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductGetController {
    private final ProductFinderByIdService productFinderByIdService;
    private final ProductMapper productMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        Product product = productFinderByIdService.findBy(id);
        ProductResponse response = productMapper.toResponse(product);
        return ResponseEntity.ok(response);
    }
}
