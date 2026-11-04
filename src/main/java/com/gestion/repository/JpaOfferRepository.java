package com.gestion.repository;

import com.gestion.model.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaOfferRepository extends JpaRepository<Offer, Long> {

    // :expired = null -> todas; true -> solo vencidas (fecha actual > end_date); false -> solo vigentes
    @Query("""
            SELECT o FROM Offer o
            WHERE o.commerce.id = :commerceId
              AND (:expired IS NULL
                   OR (:expired = TRUE AND o.endDate < CURRENT_TIMESTAMP)
                   OR (:expired = FALSE AND (o.endDate IS NULL OR o.endDate >= CURRENT_TIMESTAMP)))
            """)
    Page<Offer> findAllWithFilters(@Param("commerceId") Long commerceId,
                                   @Param("expired") Boolean expired,
                                   Pageable pageable);

}
