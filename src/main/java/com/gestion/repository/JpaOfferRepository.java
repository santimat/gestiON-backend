package com.gestion.repository;

import com.gestion.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOfferRepository extends JpaRepository<Offer, Long> {
}
