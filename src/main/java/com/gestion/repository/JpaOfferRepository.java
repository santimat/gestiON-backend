package com.gestion.repository;

import com.gestion.enums.OfferStatus;
import com.gestion.model.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface JpaOfferRepository extends JpaRepository<Offer, Long> {

    Page<Offer> findByCommerceId(Long commerceId, Pageable pageable);

    Page<Offer> findByCommerceIdAndEndDateBefore(Long commerceId, Instant threshold, Pageable pageable);

    Page<Offer> findByCommerceIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long commerceId,
                                                                                    Instant startDate,
                                                                                    Instant endDate,
                                                                                    Pageable pageable);

    Page<Offer> findByCommerceIdAndStartDateAfter(Long commerceId, Instant threshold, Pageable pageable);

    default Page<Offer> findAllWithFilters(Long commerceId, OfferStatus status, Instant now, Pageable pageable) {
        Instant normalizedNow = now.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        if (status == null) {
            return findByCommerceId(commerceId, pageable);
        }
        return switch (status) {
            case EXPIRED -> findByCommerceIdAndEndDateBefore(commerceId, normalizedNow, pageable);
            case ACTIVE ->
                    findByCommerceIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(commerceId, normalizedNow, normalizedNow, pageable);
            case SCHEDULED -> findByCommerceIdAndStartDateAfter(commerceId, normalizedNow, pageable);
        };
    }

}
