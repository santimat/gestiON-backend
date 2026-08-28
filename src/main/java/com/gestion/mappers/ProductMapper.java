package com.gestion.mappers;

import com.gestion.dto.request.product.ProductRequest;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        Product product = new Product();

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCostPrice(request.costPrice());
        product.setSalePrice(request.salePrice());
        product.setCurrentStock(request.currentStock());
        product.setMinStock(request.minStock());

        return product;
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCostPrice(),
                product.getSalePrice(),
                product.getImageUrl(),
                product.getCategory(),
                product.getStatus()
        );
    }
}
