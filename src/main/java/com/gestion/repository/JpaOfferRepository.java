package com.gestion.repository;

import com.gestion.enums.OfferStatus;
import com.gestion.enums.OfferType;
import com.gestion.enums.PaymentMethod;
import com.gestion.model.Offer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface JpaOfferRepository extends JpaRepository<Offer, Long>, JpaSpecificationExecutor<Offer> {

    Optional<Offer> findByIdAndCommerceId(Long id, Long commerceId);

    List<Offer> findOverlappingByCommerceIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long commerceId,
                                                                                               Instant startDate,
                                                                                               Instant endDate);

    @EntityGraph(attributePaths = "paymentMethods")
    Page<Offer> findByCommerceIdAndOfferType(Long commerceId, OfferType offerType, Pageable pageable);

    default Page<Offer> findAllWithFilters(Long commerceId, OfferStatus status, PaymentMethod paymentMethod,
                                           OfferType offerType, Instant now, Pageable pageable) {
        Instant normalizedNow = now.truncatedTo(java.time.temporal.ChronoUnit.MICROS);

        Specification<Offer> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("commerce").get("id"), commerceId));

            if (offerType != null) {
                predicates.add(criteriaBuilder.equal(root.get("offerType"), offerType));
            }

            if (paymentMethod != null) {
                predicates.add(criteriaBuilder.isMember(paymentMethod, root.get("paymentMethods")));
            }

            if (status != null) {
                switch (status) {
                    case EXPIRED -> predicates.add(criteriaBuilder.lessThan(root.get("endDate"), normalizedNow));
                    case ACTIVE -> predicates.add(criteriaBuilder.and(
                            criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), normalizedNow),
                            criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), normalizedNow)));
                    case SCHEDULED -> predicates.add(criteriaBuilder.greaterThan(root.get("startDate"), normalizedNow));
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return findAll(specification, pageable);
    }
}