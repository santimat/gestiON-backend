package com.gestion.repository;

import com.gestion.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaCategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findAllByCommerceId(Long commerceId, Pageable pageable);

    Optional<Category> findByIdAndCommerceId(Long id, Long commerceId);

    boolean existsByCommerceIdAndNameIgnoreCase(Long commerceId, String name);

    boolean existsByCommerceIdAndNameIgnoreCaseAndIdNot(Long commerceId, String name, Long id);
}
