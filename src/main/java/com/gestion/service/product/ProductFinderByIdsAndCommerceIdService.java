package com.gestion.service.product;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductFinderByIdsAndCommerceIdService {
    private final JpaProductRepository jpaProductRepository;

    public List<Product> findProductsByIdsAndCommerceId(List<Long> ids, Long commerceId) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<Product> products = jpaProductRepository.findByIdInAndCommerceId(ids, commerceId);

        if (products.size() != ids.size()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCTS_OFFER_NOT_FOUND, "Some products were not found for the current commerce");
        }

        return products;
    }
}