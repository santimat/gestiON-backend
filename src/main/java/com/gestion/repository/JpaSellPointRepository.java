package com.gestion.repository;

import com.gestion.model.SellPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSellPointRepository extends JpaRepository<SellPoint, Long> {

    Page<SellPoint> findAllByCommerceId(Long commerceId, Pageable pageable);

    boolean existsByCommerceIdAndNameIgnoreCase(Long commerceId, String name);
}
