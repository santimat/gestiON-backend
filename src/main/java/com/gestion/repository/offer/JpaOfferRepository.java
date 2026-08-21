package com.gestion.repository.offer;

import com.gestion.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOfferRepository extends JpaRepository<Offer, Long> {
}
