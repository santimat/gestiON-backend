package com.gestion.repository;

import com.gestion.enums.OfferStatus;
import com.gestion.enums.PaymentMethod;
import com.gestion.model.Offer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface JpaOfferRepository extends JpaRepository<Offer, Long> {

    Page<Offer> findByCommerceId(Long commerceId, Pageable pageable);

    Page<Offer> findByCommerceIdAndEndDateBefore(Long commerceId, Instant threshold, Pageable pageable);

    Page<Offer> findByCommerceIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long commerceId,
                                                                                    Instant startDate,
                                                                                    Instant endDate,
                                                                                    Pageable pageable);

    List<Offer> findOverlappingByCommerceIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long commerceId,
                                                                                               Instant startDate,
                                                                                               Instant endDate);

    Page<Offer> findByCommerceIdAndPaymentMethodsContaining(Long commerceId,
                                                            PaymentMethod paymentMethod,
                                                            Pageable pageable);

    Page<Offer> findByCommerceIdAndPaymentMethodsContainingAndEndDateBefore(Long commerceId,
                                                                            PaymentMethod paymentMethod,
                                                                            Instant threshold,
                                                                            Pageable pageable);

    Page<Offer> findByCommerceIdAndPaymentMethodsContainingAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long commerceId,
                                                                                                                PaymentMethod paymentMethod,
                                                                                                                Instant startDate,
                                                                                                                Instant endDate,
                                                                                                                Pageable pageable);

    Page<Offer> findByCommerceIdAndPaymentMethodsContainingAndStartDateAfter(Long commerceId,
                                                                            PaymentMethod paymentMethod,
                                                                            Instant threshold,
                                                                            Pageable pageable);

    Page<Offer> findByCommerceIdAndStartDateAfter(Long commerceId, Instant threshold, Pageable pageable);

    default Page<Offer> findAllWithFilters(Long commerceId, OfferStatus status, PaymentMethod paymentMethod, Instant now, Pageable pageable) {
        Instant normalizedNow = now.truncatedTo(java.time.temporal.ChronoUnit.MICROS);

        if (paymentMethod == null) {
            if (status == null) {
                return findByCommerceId(commerceId, pageable);
            }
            return switch (status) {
                case EXPIRED -> findByCommerceIdAndEndDateBefore(commerceId, normalizedNow, pageable);
                case ACTIVE -> findByCommerceIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(commerceId, normalizedNow, normalizedNow, pageable);
                case SCHEDULED -> findByCommerceIdAndStartDateAfter(commerceId, normalizedNow, pageable);
            };
        }

        if (status == null) {
            return findByCommerceIdAndPaymentMethodsContaining(commerceId, paymentMethod, pageable);
        }
        return switch (status) {
            case EXPIRED -> findByCommerceIdAndPaymentMethodsContainingAndEndDateBefore(commerceId, paymentMethod, normalizedNow, pageable);
            case ACTIVE -> findByCommerceIdAndPaymentMethodsContainingAndStartDateLessThanEqualAndEndDateGreaterThanEqual(commerceId, paymentMethod, normalizedNow, normalizedNow, pageable);
            case SCHEDULED -> findByCommerceIdAndPaymentMethodsContainingAndStartDateAfter(commerceId, paymentMethod, normalizedNow, pageable);
        };
    }

}
