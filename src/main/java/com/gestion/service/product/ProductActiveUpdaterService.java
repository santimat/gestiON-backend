package com.gestion.service.product;

import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductActiveUpdaterService {
    private final ProductFinderByIdAndCommerceIdService productFinderByIdAndCommerceIdService;
    private final JpaProductRepository productRepository;

    @Transactional
    public ProductUpdateActiveResponse toggleActive(Long productId, Long commerceId) {
        Product productToUpdate = productFinderByIdAndCommerceIdService.findProductByIdAndCommerceId(productId,
                commerceId);
        productToUpdate.setActive(!productToUpdate.isActive());
        Product updatedProduct = productRepository.save(productToUpdate);
        return new ProductUpdateActiveResponse(
                updatedProduct.getId(),
                updatedProduct.isActive()
        );
    }
}
