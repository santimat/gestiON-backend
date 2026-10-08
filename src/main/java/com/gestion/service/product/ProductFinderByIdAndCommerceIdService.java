package com.gestion.service.product;

import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductFinderByIdAndCommerceIdService {
    private final JpaProductRepository jpaProductRepository;

    public Product findProductByIdAndCommerceId(Long id, Long commerceId) {
        return jpaProductRepository.findByIdAndCommerceId(id, commerceId)
                .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
    }
}
