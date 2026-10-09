package com.gestion.repository;

import com.gestion.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaCategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findAllByCommerceId(Long commerceId, org.springframework.data.domain.Pageable pageable);

    Optional<Category> findByIdAndCommerceId(Long id, Long commerceId);
}
