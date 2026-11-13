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
import java.time.temporal.ChronoUnit;
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

        // se normaliz la fecha, porque algunas base de datos no trabajan igual que Java con la precisión de los
        // instantes
        Instant normalizedNow = now.truncatedTo(ChronoUnit.MICROS);

        // root -> entidad
        // query -> consulta (aplicar metodos de ordenamiento, paginación, etc)
        // criteriaBuilder -> constructor de criterios (equal, greaterThan, lessThan, etc)
        Specification<Offer> specification = (root, query, criteriaBuilder) -> {

            // Lista de condiciones para la query
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("commerce").get("id"), commerceId));

            if (offerType != null) {
                predicates.add(criteriaBuilder.equal(root.get("offerType"), offerType));
            }

            if (paymentMethod != null) {
                // isMember -> verifica si un elemento está presente en una colección
                predicates.add(criteriaBuilder.isMember(paymentMethod, root.get("paymentMethods")));
            }

            if (status != null) {
                switch (status) {
                    // momento de la consulta > fecha de finalización
                    case EXPIRED -> predicates.add(criteriaBuilder.lessThan(root.get("endDate"), normalizedNow));
                    // rango de fechas: fecha de inicio <= momento de la consulta <= fecha de finalización
                    case ACTIVE -> predicates.add(criteriaBuilder.and(
                            criteriaBuilder.lessThanOrEqualTo(root.get("startDate"), normalizedNow),
                            criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), normalizedNow)));
                    //startDate > momento de la consulta
                    case SCHEDULED -> predicates.add(criteriaBuilder.greaterThan(root.get("startDate"), normalizedNow));
                }
            }

            // lo aplicamos como un AND, es decir, todas las condiciones deben cumplirse
            return criteriaBuilder.and(predicates.toArray(
                    // referencia para que toArray sepa el tipo de array que debe crear
                    new Predicate[0]
            ));
        };

        // all lo que cumpla las condiciones de la especificación, se devuelve paginado
        return findAll(specification, pageable);
    }
}