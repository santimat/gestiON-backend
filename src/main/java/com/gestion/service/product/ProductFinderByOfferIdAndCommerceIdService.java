package com.gestion.service.product;

import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductFinderByOfferIdAndCommerceIdService {
    private final JpaProductRepository jpaProductRepository;

    public List<Product> findProductsByOfferIdAndCommerceId(Long offerId, Long commerceId) {
        if (offerId == null) {
            return List.of();
        }
        return jpaProductRepository.findByOfferIdAndCommerceId(offerId, commerceId);
    }
}
