package com.gestion.repository;

import com.gestion.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndCommerceId(Long id, Long commerceId);

    @Query("""
            SELECT p FROM Product p
            WHERE p.commerce.id = :commerceId
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:searchText IS NULL
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchText, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchText, '%')))
            """)
    Page<Product> findAllWithFilters(@Param("commerceId") Long commerceId,
                                     @Param("categoryId") Long categoryId,
                                     @Param("searchText") String searchText,
                                     Pageable pageable);
}
