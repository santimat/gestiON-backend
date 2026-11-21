package com.gestion.service.product;

import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.enums.StockStatus;
import com.gestion.mappers.CategoryMapper;
import com.gestion.mappers.ProductMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Product;
import com.gestion.properties.MinioProperties;
import com.gestion.repository.JpaProductRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import com.gestion.service.file.FileFinderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ProductSearcherService {
    private final JpaProductRepository jpaProductRepository;
    private final ProductSuggestedPriceCalculatorService productSuggestedPriceCalculatorService;
    private final FileFinderService fileFinderService;
    private final MinioProperties minioProperties;
    private final CommerceFinderByIdService commerceFinderByIdService;

    public Page<ProductResponse> searchProducts(Long commerceId, Long categoryId, String searchText,
                                                Boolean active, StockStatus stockStatus, Pageable pageable) {
        if (searchText != null && searchText.isBlank()) {
            searchText = null;
        }

        String stockStatusStr = stockStatus != null ? stockStatus.name() : null;

        Page<Product> products = jpaProductRepository
                .findAllWithFilters(commerceId, categoryId, searchText, active, stockStatusStr, pageable);
        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);

        return products.map(product -> {
            String productImageUrl = fileFinderService.getObjectUrl(product.getImageName(),
                    minioProperties.dir().productImages());

            BigDecimal suggestedPrice = productSuggestedPriceCalculatorService.calculateSuggestedPrice(product, commerce);
            CategoryResponse categoryResponse = CategoryMapper.toResponse(product.getCategory());
            return ProductMapper.toResponse(product, productImageUrl, suggestedPrice, categoryResponse);
        });
    }
}
