package com.gestion.repository;

import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;
import com.gestion.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface JpaSaleRepository extends JpaRepository<Sale, Long> {

    @Query("""
            SELECT s FROM Sale s
            LEFT JOIN FETCH s.client c
            WHERE s.commerce.id = :commerceId
              AND (:clientId IS NULL OR s.client.id = :clientId)
              AND (:status IS NULL OR s.status = :status)
              AND (:paymentMethod IS NULL OR s.paymentMethod = :paymentMethod)
              AND (:searchText IS NULL
                   OR LOWER(s.observations) LIKE LOWER(CONCAT('%', :searchText, '%'))
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :searchText, '%')))
            """)
    Page<Sale> findAllWithFilters(@Param("commerceId") Long commerceId,
                                  @Param("clientId") Long clientId,
                                  @Param("status") SaleStatus status,
                                  @Param("paymentMethod") PaymentMethod paymentMethod,
                                  @Param("searchText") String searchText,
                                  Pageable pageable);

    @Query("""
            SELECT COUNT(s) FROM Sale s
            WHERE s.client.id = :clientId
              AND s.commerce.id = :commerceId
              AND s.status = com.gestion.enums.SaleStatus.PAYED
            """)
    long countPaidSalesByClient(@Param("clientId") Long clientId,
                                @Param("commerceId") Long commerceId);

    @Query("""
            SELECT COUNT(s) FROM Sale s
            WHERE s.client.id = :clientId
              AND s.commerce.id = :commerceId
              AND s.status = com.gestion.enums.SaleStatus.PENDING
            """)
    long countPendingSalesByClient(@Param("clientId") Long clientId,
                                   @Param("commerceId") Long commerceId);

    @Query("""
            SELECT COALESCE(SUM(s.total), 0) FROM Sale s
            WHERE s.client.id = :clientId
              AND s.commerce.id = :commerceId
              AND s.status = com.gestion.enums.SaleStatus.PAYED
            """)
    BigDecimal sumPaidSalesTotalByClient(@Param("clientId") Long clientId,
                                         @Param("commerceId") Long commerceId);

    @Query("""
            SELECT COALESCE(SUM(s.total), 0) FROM Sale s
            WHERE s.client.id = :clientId
              AND s.commerce.id = :commerceId
              AND s.status = com.gestion.enums.SaleStatus.PENDING
            """)
    BigDecimal sumPendingSalesTotalByClient(@Param("clientId") Long clientId,
                                            @Param("commerceId") Long commerceId);

    default ClientSalesSummary countSalesAndSpentByClient(Long clientId, Long commerceId) {
        long paidCount = countPaidSalesByClient(clientId, commerceId);
        long pendingCount = countPendingSalesByClient(clientId, commerceId);
        BigDecimal totalSpent = sumPaidSalesTotalByClient(clientId, commerceId);
        BigDecimal totalPending = sumPendingSalesTotalByClient(clientId, commerceId);

        return new ClientSalesSummary() {
            @Override
            public Number getPaidSalesCount() {
                return paidCount;
            }

            @Override
            public Number getPendingSalesCount() {
                return pendingCount;
            }

            @Override
            public BigDecimal getTotalSpent() {
                return totalSpent;
            }

            @Override
            public BigDecimal getTotalPending() {
                return totalPending;
            }
        };
    }

    // @EntityGraph para cargar las relaciones junto con la entidad Sale, evitando el problema de N+1
    @EntityGraph(attributePaths = {"client", "sellPoint", "user"})
    Optional<Sale> findByIdAndCommerceId(Long id, Long commerceId);

    interface ClientSalesSummary {
        Number getPaidSalesCount();

        Number getPendingSalesCount();

        BigDecimal getTotalSpent();

        BigDecimal getTotalPending();
    }
}