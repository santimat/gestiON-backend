package com.gestion.service.product;

import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductDeleterService {
    private final JpaProductRepository jpaProductRepository;
    private final ProductFinderByIdService productFinderByService;

    public void delete(Long id){
        Product product = productFinderByService.findBy(id);
        jpaProductRepository.delete(product);
    }
}
