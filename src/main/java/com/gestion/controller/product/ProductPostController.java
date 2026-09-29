package com.gestion.controller.product;

import com.gestion.service.product.ProductCreatorService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
public class ProductPostController {
    private final ProductCreatorService productCreatorService;

//    @PostMapping
//    public ResponseEntity<ProductResponse> create(ProductRequest request, Long commerceId) {
//        Product product = productCreatorService.createProduct(request, commerceId);
//        ProductResponse response = ProductMapper.toResponse(product);
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
}
