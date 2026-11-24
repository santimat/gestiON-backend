package com.gestion.mappers;

import com.gestion.dto.request.product.ProductRequest;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.dto.response.product.ProductWithOfferResponse;
import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.model.Offer;
import com.gestion.model.Product;

import java.math.BigDecimal;

public class ProductMapper {

    public static Product toEntity(ProductRequest request, Commerce commerce, Category category) {
        Product product = new Product();

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCostPrice(request.costPrice());
        product.setSalePrice(request.salePrice());
        product.setProfitMultiplier(request.profitMultiplier());
        product.setCurrentStock(request.currentStock());
        product.setMinStock(request.minStock());
        product.setCommerce(commerce);
        product.setCategory(category);
        return product;
    }

    public static ProductResponse toResponse(Product product,
                                             String imageUrl,
                                             BigDecimal suggestedPrice,
                                             CategoryResponse categoryResponse) {
        if (product == null) {
            return null;
        }
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCostPrice(),
                product.getSalePrice(),
                suggestedPrice,
                product.getProfitMultiplier(),
                product.getCurrentStock(),
                product.getMinStock(),
                imageUrl,
                product.getOffer() != null ? product.getOffer().getId() : null,
                categoryResponse,
                product.isActive(),
                product.getUpdatedAt()
        );
    }

    public static ProductWithOfferResponse toWithOfferResponse(Product product,
                                                               String imageUrl,
                                                               BigDecimal suggestedPrice,
                                                               CategoryResponse categoryResponse,
                                                               Offer offer,
                                                               BigDecimal discountedPrice) {
        if (product == null) {
            return null;
        }
        return new ProductWithOfferResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCostPrice(),
                product.getSalePrice(),
                suggestedPrice,
                product.getProfitMultiplier(),
                product.getCurrentStock(),
                product.getMinStock(),
                imageUrl,
                offer != null ? offer.getId() : null,
                offer != null ? offer.getValue() : null,
                offer != null ? offer.getDiscountType() : null,
                discountedPrice,
                categoryResponse,
                product.isActive(),
                product.getUpdatedAt()
        );
    }
}
