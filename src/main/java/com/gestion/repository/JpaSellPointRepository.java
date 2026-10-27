package com.gestion.repository;

import com.gestion.model.SellPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaSellPointRepository extends JpaRepository<SellPoint, Long> {

    Page<SellPoint> findAllByCommerceId(Long commerceId, Pageable pageable);

    Optional<SellPoint> findByIdAndCommerceId(Long id, Long commerceId);

    boolean existsByCommerceIdAndNameIgnoreCase(Long commerceId, String name);

    boolean existsByCommerceIdAndNameIgnoreCaseAndIdNot(Long commerceId, String name, Long id);
}
