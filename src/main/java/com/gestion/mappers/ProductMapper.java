package com.gestion.mappers;

import com.gestion.dto.request.product.ProductRequest;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.model.Product;

import java.math.BigDecimal;

public class ProductMapper {

    public static Product toEntity(ProductRequest request, Commerce commerce, Category category) {
        Product product = new Product();

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCostPrice(request.costPrice());
        product.setCurrentStock(request.currentStock());
        product.setMinStock(request.minStock());
        product.setCommerce(commerce);
        product.setCategory(category);
        return product;
    }

    public static ProductResponse toResponse(Product product, String imageUrl, BigDecimal salePrice, CategoryResponse categoryResponse) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCostPrice(),
                salePrice,
                product.getCurrentStock(),
                product.getMinStock(),
                imageUrl != null ? imageUrl : product.getImageName(),
                categoryResponse,
                product.isActive(),
                product.getUpdatedAt()
        );
    }
}
