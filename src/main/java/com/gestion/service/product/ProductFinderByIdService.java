package com.gestion.service.product;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductFinderByIdService {
    private final JpaProductRepository jpaProductRepository;

    public Product findProductById(Long id) {
        return jpaProductRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product with id " + id + " not found"));
    }
}
