package com.gestion.repository;

import com.gestion.model.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOfferRepository extends JpaRepository<Offer, Long> {

    Page<Offer> findAllByCommerceId(Long commerceId, Pageable pageable);
}
