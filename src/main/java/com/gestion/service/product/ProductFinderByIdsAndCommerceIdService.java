package com.gestion.service.product;

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
        return jpaProductRepository.findByIdInAndCommerceId(ids, commerceId);
    }
}