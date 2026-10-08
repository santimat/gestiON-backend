package com.gestion.service.product;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.mappers.ProductMapper;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import com.gestion.service.file.FileFinderService;
import io.minio.errors.MinioException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class ProductSearcherService {
    private final JpaProductRepository jpaProductRepository;
    private final ProductSuggestedPriceCalculatorService productSuggestedPriceCalculatorService;
    private final FileFinderService fileFinderService;

    public Page<ProductResponse> searchProducts(UserPrincipal authenticatedUser, Pageable pageable) {
        Page<Product> products = jpaProductRepository.findAllByCommerceId(authenticatedUser.getCommerceId(), pageable);
        return products.map(product -> {
            String productImageUrl = null;
            try {
                if (product.getImageName() != null) {
                    productImageUrl = fileFinderService.getObjectUrl(product.getImageName(), "products");
                }
            } catch (MinioException e) {
                System.out.println("Error retrieving business logo from MinIO: " + e.getCause());
            }
            Double profitMultiplier = product.getProfitMultiplier() != null ? product.getProfitMultiplier() :
                    product.getCommerce().getProfitMultiplier();
            BigDecimal suggestedPrice = productSuggestedPriceCalculatorService.calculateSuggestedPrice(product.getCostPrice(), profitMultiplier);
            CategoryResponse categoryResponse = CategoryMapper.toResponse(product.getCategory());
            return ProductMapper.toResponse(product, productImageUrl, suggestedPrice, categoryResponse);
        });
    }
}
