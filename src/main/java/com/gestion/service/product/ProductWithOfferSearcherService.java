package com.gestion.service.product;

import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.dto.response.product.ProductWithOfferResponse;
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
public class ProductWithOfferSearcherService {
    private final JpaProductRepository jpaProductRepository;
    private final ProductSuggestedPriceCalculatorService suggestedPriceCalculator;
    private final FileFinderService fileFinder;
    private final MinioProperties minioProperties;
    private final CommerceFinderByIdService commerceFinder;
    private final ProductPriceWithOfferResolver priceWithOfferResolver;

    public Page<ProductWithOfferResponse> searchProductsWithOffer(Long commerceId, Long categoryId, String searchText,
                                                                  Boolean active, Pageable pageable) {
        if (searchText != null && searchText.isBlank()) {
            searchText = null;
        }

        Page<Product> products = jpaProductRepository
                .findAllWithFilters(commerceId, categoryId, searchText, active, pageable);
        Commerce commerce = commerceFinder.findCommerceById(commerceId);

        return products.map(product -> {
            String productImageUrl = fileFinder.getObjectUrl(product.getImageName(),
                    minioProperties.dir().productImages());

            BigDecimal suggestedPrice = suggestedPriceCalculator.calculateSuggestedPrice(product, commerce);
            CategoryResponse categoryResponse = CategoryMapper.toResponse(product.getCategory());

            ProductPriceWithOfferResolver.PriceWithOfferResult priceResult = priceWithOfferResolver.resolve(product);

            return ProductMapper.toWithOfferResponse(
                    product, productImageUrl, suggestedPrice, categoryResponse,
                    priceResult.activeOffer(), priceResult.discountedPrice());
        });
    }
}