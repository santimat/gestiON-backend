package com.gestion.service.product;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductFinderByIdAndCommerceIdService {
    private final JpaProductRepository jpaProductRepository;

    public Product findProductByIdAndCommerceId(Long id, Long commerceId) {
        return jpaProductRepository.findByIdAndCommerceId(id, commerceId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product with id " + id + " not found"));
    }

    public Map<Long, Product> findProductsByIdsAndCommerceId(List<Long> ids, Long commerceId) {
        List<Product> products = jpaProductRepository.findByIdInAndCommerceId(ids, commerceId);
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
        
        for (Long id : ids) {
            if (!productMap.containsKey(id)) {
                throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Product with id " + id + " not found");
            }
        }
        return productMap;
    }
}
