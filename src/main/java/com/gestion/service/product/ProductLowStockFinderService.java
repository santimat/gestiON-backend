package com.gestion.service.product;

import com.gestion.dto.response.product.ProductLowStockResponse;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductLowStockFinderService {

    private final JpaProductRepository jpaProductRepository;

    public List<ProductLowStockResponse> findLowStockProducts(Long commerceId) {
        return jpaProductRepository.findLowStockProductsByCommerceId(commerceId);
    }
}