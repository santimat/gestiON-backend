package com.gestion.repository;

import com.gestion.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByCommerceId(Long commerceId, Pageable pageable);

    Optional<Product> findByIdAndCommerceId(Long id, Long commerceId);
}
