package com.gestion.service.product;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.category.CategoryResponse;
import com.gestion.dto.response.product.ProductResponse;
import com.gestion.mappers.CategoryMapper;
import com.gestion.mappers.ProductMapper;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import com.gestion.service.file.FileFinderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class ProductSearcherService {
    private final JpaProductRepository jpaProductRepository;
    private final FileFinderService fileFinderService;

    public Page<ProductResponse> searchProducts(UserPrincipal authenticatedUser, Pageable pageable) {
        Page<Product> products = jpaProductRepository.findAllByCommerceId(authenticatedUser.getCommerceId(), pageable);
        return products.map(product -> {
            String productImageUrl = product.getImageName() != null ?
                    fileFinderService.getObjectUrl(product.getImageName(), "product-images") : null;
            // TODO: preguntar de donde obtener el precio de venta, si es un campo de la entidad Product o si se calcula de alguna manera
            BigDecimal salePrice = product.getCostPrice().multiply(BigDecimal.valueOf(1.5));
            CategoryResponse categoryResponse = CategoryMapper.toResponse(product.getCategory());
            return ProductMapper.toResponse(product, productImageUrl, salePrice, categoryResponse);
        });
    }
}
