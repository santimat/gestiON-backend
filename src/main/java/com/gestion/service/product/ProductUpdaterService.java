package com.gestion.service.product;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.product.ProductUpdateRequest;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.mappers.ProductMapper;
import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.model.Product;
import com.gestion.properties.MinioProperties;
import com.gestion.repository.JpaProductRepository;
import com.gestion.service.category.CategoryFinderByIdAndCommerceIdService;
import com.gestion.service.commerce.CommerceFinderByIdService;
import com.gestion.service.file.FileFinderService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class ProductUpdaterService {

    private final ProductFinderByIdAndCommerceIdService productFinderByIdAndCommerceIdService;
    private final CategoryFinderByIdAndCommerceIdService categoryFinderByIdAndCommerceIdService;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final ProductSuggestedPriceCalculatorService productSuggestedPriceCalculatorService;
    private final JpaProductRepository productRepository;
    private final ProductImageUploaderService productImageUploaderService;
    private final FileFinderService fileFinderService;
    private final MinioProperties minioProperties;

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductUpdateRequest request, UserPrincipal authenticatedUser) {
        Product product = productFinderByIdAndCommerceIdService
                .findProductByIdAndCommerceId(productId, authenticatedUser.getCommerceId());
        Commerce commerce = commerceFinderByIdService.findCommerceById(authenticatedUser.getCommerceId());
        Category category = categoryFinderByIdAndCommerceIdService
                .findCategoryByIdAndCommerceId(request.categoryId(), authenticatedUser.getCommerceId());

        product.setName(request.name());
        product.setDescription(request.description());
        product.setCostPrice(request.costPrice());
        product.setSalePrice(request.salePrice());
        product.setProfitMultiplier(request.profitMultiplier());
        product.setCurrentStock(request.currentStock());
        product.setMinStock(request.minStock());
        product.setCategory(category);
        Product updatedProduct = productRepository.save(product);

        if (request.image() != null) {
            productImageUploaderService.uploadProductImage(request.image(), updatedProduct);
        }

        String productImageUrl = fileFinderService.getObjectUrl(updatedProduct.getImageName(),
                minioProperties.dir().productImages());

        BigDecimal suggestedPrice = productSuggestedPriceCalculatorService.calculateSuggestedPrice(updatedProduct, commerce);
        return ProductMapper.toResponse(updatedProduct, productImageUrl, suggestedPrice,
                CategoryMapper.toResponse(category));
    }
}
