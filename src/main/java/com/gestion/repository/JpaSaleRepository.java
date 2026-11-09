package com.gestion.repository;

import com.gestion.dto.response.sale.PeriodProductCount;
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
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    default BigDecimal getProfitByPeriod(Long commerceId, LocalDate startDate, LocalDate endDate) {
        ZoneId zone = ZoneOffset.UTC;
        Instant start = startDate.atStartOfDay(zone).toInstant();
        Instant end = startDate.equals(endDate)
                ? startDate.plusDays(1).atStartOfDay(zone).toInstant()
                : endDate.atStartOfDay(zone).toInstant();
        return sumPayedTotalByCommerceBetween(commerceId, start, end);
    }


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

    default long getSalesCountByPeriod(Long commerceId, LocalDate startDate, LocalDate endDate) {
        ZoneId zone = ZoneOffset.UTC;
        Instant start = startDate.atStartOfDay(zone).toInstant();
        Instant end = startDate.equals(endDate)
                ? startDate.plusDays(1).atStartOfDay(zone).toInstant()
                : endDate.atStartOfDay(zone).toInstant();
        return countSalesByPeriod(commerceId, start, end);
    }


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

    default BigDecimal getAverageSaleValueByPeriod(Long commerceId, LocalDate startDate, LocalDate endDate) {
        ZoneId zone = ZoneOffset.UTC;
        Instant start = startDate.atStartOfDay(zone).toInstant();
        Instant end = startDate.equals(endDate)
                ? startDate.plusDays(1).atStartOfDay(zone).toInstant()
                : endDate.atStartOfDay(zone).toInstant();
        Double avg = averageSaleValueByPeriod(commerceId, start, end);
        return avg == null ? BigDecimal.ZERO : BigDecimal.valueOf(avg);
    }


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

    default List<PeriodProductCount> getProductsSoldByPeriod(
            Long commerceId, LocalDate startDate, LocalDate endDate) {
        ZoneId zone = ZoneOffset.UTC;

        Instant start = startDate.atStartOfDay(zone).toInstant();
        Instant end = startDate.equals(endDate)
                ? startDate.plusDays(1).atStartOfDay(zone).toInstant()
                : endDate.atStartOfDay(zone).toInstant();

        List<SaleDetail> details = saleDetailsInPeriod(commerceId, start, end);

        boolean byHour = startDate.equals(endDate);
        boolean byWeek = !byHour && ChronoUnit.DAYS.between(startDate, endDate) + 1 <= 7;
        // fallback to week when exactly up to 7 calendar days, otherwise month

        if (byHour) {
            Map<Integer, Long> grouped = details.stream()
                    .collect(Collectors.groupingBy(
                            d -> d.getSale().getCreatedAt().atZone(zone).getHour(),
                            Collectors.summingLong(SaleDetail::getQuantity)));
            List<PeriodProductCount> result = new ArrayList<>();
            for (int hour = 0; hour < 24; hour++) {
                result.add(new PeriodProductCount(
                        String.format("%02d:00", hour),
                        grouped.getOrDefault(hour, 0L)));
            }
            return result;
        }

        if (byWeek) {
            Map<DayOfWeek, Long> grouped = details.stream()
                    .collect(Collectors.groupingBy(
                            d -> d.getSale().getCreatedAt().atZone(zone).getDayOfWeek(),
                            Collectors.summingLong(SaleDetail::getQuantity)));
            List<PeriodProductCount> result = new ArrayList<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                result.add(new PeriodProductCount(
                        day.name(),
                        grouped.getOrDefault(day, 0L)));
            }
            return result;
        }

        Map<Integer, Long> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getSale().getCreatedAt().atZone(zone).get(ChronoField.ALIGNED_WEEK_OF_MONTH),
                        Collectors.summingLong(SaleDetail::getQuantity)));
        java.util.List<PeriodProductCount> result = new java.util.ArrayList<>();
        for (int week = 1; week <= 4; week++) {
            result.add(new PeriodProductCount(
                    "Week " + week,
                    grouped.getOrDefault(week, 0L)));
        }
        return result;
    }

    interface ClientSalesSummary {
        Number getPaidSalesCount();

        Number getPendingSalesCount();

        BigDecimal getTotalSpent();

        BigDecimal getTotalPending();
    }
}