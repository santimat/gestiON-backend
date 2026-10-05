package com.gestion.repository;

import com.gestion.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findAllByCommerceId(Long commerceId, org.springframework.data.domain.Pageable pageable);
}
