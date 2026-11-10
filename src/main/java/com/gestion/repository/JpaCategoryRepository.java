package com.gestion.repository;

import com.gestion.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaCategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findAllByCommerceId(Long commerceId, Pageable pageable);

    @Query("""
            SELECT c FROM Category c
            WHERE c.commerce.id = :commerceId
              AND (:categoryName IS NULL
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%')))
            """)
    Page<Category> findAllWithFilters(@Param("commerceId") Long commerceId,
                                     @Param("categoryName") String categoryName,
                                     Pageable pageable);

    Optional<Category> findByIdAndCommerceId(Long id, Long commerceId);

    boolean existsByCommerceIdAndNameIgnoreCase(Long commerceId, String name);

    boolean existsByCommerceIdAndNameIgnoreCaseAndIdNot(Long commerceId, String name, Long id);
}
