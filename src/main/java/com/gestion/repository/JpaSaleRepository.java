package com.gestion.repository;

import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;
import com.gestion.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JpaSaleRepository extends JpaRepository<Sale, Long> {

    @Query("""
            SELECT s FROM Sale s
            WHERE s.commerce.id = :commerceId
              AND (:clientId IS NULL OR s.client.id = :clientId)
              AND (:status IS NULL OR s.status = :status)
              AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod)
              AND (:searchText IS NULL
                   OR LOWER(s.observations) LIKE LOWER(CONCAT('%', :searchText, '%'))
                   OR LOWER(s.client.name) LIKE LOWER(CONCAT('%', :searchText, '%')))
            """)
    Page<Sale> findAllWithFilters(@Param("commerceId") Long commerceId,
                                  @Param("clientId") Long clientId,
                                  @Param("status") SaleStatus status,
                                  @Param("paymentMethod") PaymentMethod paymentMethod,
                                  @Param("searchText") String searchText,
                                  Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"client", "sellPoint", "user"})
    Optional<Sale> findByIdAndCommerceId(Long id, Long commerceId);
}
