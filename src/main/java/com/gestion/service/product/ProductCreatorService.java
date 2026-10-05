package com.gestion.service.product;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.product.ProductRequest;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.mappers.ProductMapper;
import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import com.gestion.service.category.CategoryFinderByIdService;
import com.gestion.service.commerce.CommerceFinderByIdService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductCreatorService {
    private final JpaProductRepository productRepository;
    private final CategoryFinderByIdService categoryFinderByIdService;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final ProductImageUploaderService productImageUploaderService;

    public ProductResponse createProduct(ProductRequest request, UserPrincipal authenticatedUser) {
        Commerce commerce = commerceFinderByIdService.findById(authenticatedUser.getCommerceId());
        Category category = categoryFinderByIdService.findCategoryById(request.categoryId());

        Product product = ProductMapper.toEntity(request, commerce, category);
        Product newProduct = productRepository.save(product);

        String imageUrl = null;

        if (request.image() != null) {
            imageUrl = productImageUploaderService.uploadProductImage(request.image(), newProduct);
        }

        return ProductMapper.toResponse(newProduct, imageUrl, request.salePrice(), CategoryMapper.toResponse(category));
    }
}
