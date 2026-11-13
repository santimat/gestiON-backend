package com.gestion.repository;

import com.gestion.dto.project.ClientAccountSummary;
import com.gestion.dto.response.sale.TopProductsSoldResponse;
import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;
import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
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


    @EntityGraph(attributePaths = {"client", "sellPoint", "user"})
    @Query("""
            SELECT s FROM Sale s
            WHERE s.commerce.id = :commerceId
              AND s.client.id = :clientId
              AND s.status = com.gestion.enums.SaleStatus.PENDING
            """)
    Page<Sale> findPendingByClientIdAndCommerceId(@Param("commerceId") Long commerceId,
                                                  @Param("clientId") Long clientId,
                                                  Pageable pageable);

    @Query("""
            SELECT new com.gestion.dto.project.ClientAccountSummary(
                s.client.id,
                SUM(CASE WHEN s.status = com.gestion.enums.SaleStatus.PAYED THEN 1 ELSE 0 END),
                SUM(CASE WHEN s.status = com.gestion.enums.SaleStatus.PENDING THEN 1 ELSE 0 END),
                SUM(CASE WHEN s.status = com.gestion.enums.SaleStatus.PAYED THEN s.total END),
                SUM(CASE WHEN s.status = com.gestion.enums.SaleStatus.PENDING THEN s.total END))
            FROM Sale s
            WHERE s.commerce.id = :commerceId
              AND s.client.id IN :clientIds
            GROUP BY s.client.id
            """)
    List<ClientAccountSummary> findAccountSummariesByClientIds(
            @Param("clientIds") Collection<Long> clientIds,
            @Param("commerceId") Long commerceId);

    // @EntityGraph para cargar las relaciones junto con la entidad Sale, evitando el problema de N+1
    @EntityGraph(attributePaths = {"client", "sellPoint", "user"})
    Optional<Sale> findByIdAndCommerceId(Long id, Long commerceId);

    @Query("""
            SELECT COALESCE(SUM(s.total), 0) FROM Sale s
            WHERE s.commerce.id = :commerceId
              AND s.status = com.gestion.enums.SaleStatus.PAYED
              AND s.createdAt >= :start
              AND s.createdAt < :end
            """)
    BigDecimal sumPayedTotalByCommerceBetween(@Param("commerceId") Long commerceId,
                                              @Param("start") java.time.Instant start,
                                              @Param("end") java.time.Instant end);

    @Query("""
            SELECT COUNT(s) FROM Sale s
            WHERE s.commerce.id = :commerceId
              AND s.status = com.gestion.enums.SaleStatus.PAYED
              AND s.createdAt >= :start
              AND s.createdAt < :end
            """)
    long countSalesByPeriod(@Param("commerceId") Long commerceId,
                            @Param("start") Instant start,
                            @Param("end") Instant end);

    @Query("""
            SELECT COALESCE(AVG(s.total), 0) FROM Sale s
            WHERE s.commerce.id = :commerceId
              AND s.status = com.gestion.enums.SaleStatus.PAYED
              AND s.createdAt >= :start
              AND s.createdAt < :end
            """)
    Double averageSaleValueByPeriod(@Param("commerceId") Long commerceId,
                                    @Param("start") java.time.Instant start,
                                    @Param("end") java.time.Instant end);

    @Query("""
            SELECT d FROM SaleDetail d
            JOIN FETCH d.sale s
            WHERE s.commerce.id = :commerceId
              AND s.status = SaleStatus.PAYED
              AND s.createdAt >= :start
              AND s.createdAt < :end
            """)
    List<SaleDetail> saleDetailsInPeriod(@Param("commerceId") Long commerceId,
                                         @Param("start") Instant start,
                                         @Param("end") Instant end);

    @Query("""
            SELECT new com.gestion.dto.response.sale.TopProductsSoldResponse(
                p.id,
                p.name,
                SUM(sd.quantity)
            )
            FROM SaleDetail sd
            JOIN sd.sale s
            JOIN sd.product p
            WHERE s.commerce.id = :commerceId
              AND s.status = com.gestion.enums.SaleStatus.PAYED
              AND s.createdAt >= :start
              AND s.createdAt < :end
            GROUP BY p.id, p.name
            ORDER BY SUM(sd.quantity) DESC
            """)
    List<TopProductsSoldResponse> topProductsSoldByCommerceId(@Param("commerceId") Long commerceId,
                                                              @Param("start") Instant start,
                                                              @Param("end") Instant end,
                                                              Pageable pageable);

}