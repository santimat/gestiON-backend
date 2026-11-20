package com.gestion.service.product;

import com.gestion.dto.request.product.ProductUpdateRequest;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.mappers.ProductMapper;
import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import com.gestion.service.category.CategoryFinderByIdAndCommerceIdService;
import com.gestion.service.commerce.CommerceFinderByIdService;
import com.gestion.service.product.ProductSuggestedPriceCalculatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class ProductUpdaterService {
    private final ProductUpdateValidator validator;
    private final ProductImageUpdater imageUpdater;
    private final ProductSuggestedPriceCalculatorService suggestedPriceCalculator;
    private final JpaProductRepository productRepository;

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request, Long commerceId) {
        ProductUpdateValidationResult validation = validator.validate(productId, request, commerceId);
        Product product = validation.product();
        Commerce commerce = validation.commerce();
        Category category = validation.category();

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCostPrice(request.costPrice());
        product.setSalePrice(request.salePrice());
        product.setProfitMultiplier(request.profitMultiplier());
        product.setCurrentStock(request.currentStock());
        product.setMinStock(request.minStock());
        product.setCategory(category);

        Product updatedProduct = productRepository.save(product);

        String productImageUrl = imageUpdater.updateImageIfPresent(request.image(), updatedProduct);

        BigDecimal suggestedPrice = suggestedPriceCalculator.calculateSuggestedPrice(updatedProduct, commerce);
        return ProductMapper.toResponse(updatedProduct, productImageUrl, suggestedPrice, CategoryMapper.toResponse(category));
    }
}