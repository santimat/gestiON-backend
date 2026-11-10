package com.gestion.repository;

import com.gestion.dto.project.PeriodRange;
import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.enums.PaymentMethod;
import com.gestion.enums.PeriodGranularity;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface JpaSaleRepository extends JpaRepository<Sale, Long> {

    ZoneId BUSINESS_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

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

    default PeriodRange resolveRange(PeriodGranularity granularity) {
        ZoneId zone = BUSINESS_ZONE;
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(zone);
        LocalDate startDate;

        switch (granularity) {
            case DAY -> startDate = today;
            case WEEK -> startDate = today.minusDays(6);
            case MONTH -> startDate = today.minusDays(29);
            default -> throw new IllegalArgumentException("Unsupported granularity: " + granularity);
        }

        Instant start = startDate.atStartOfDay(zone).toInstant();
        return new PeriodRange(start, now, startDate, today);
    }

    default BigDecimal getProfitByPeriod(Long commerceId, PeriodGranularity granularity) {
        PeriodRange range = resolveRange(granularity);
        return sumPayedTotalByCommerceBetween(commerceId, range.start(), range.end());
    }

    default long getSalesCountByPeriod(Long commerceId, PeriodGranularity granularity) {
        PeriodRange range = resolveRange(granularity);
        return countSalesByPeriod(commerceId, range.start(), range.end());
    }

    default BigDecimal getAverageSaleValueByPeriod(Long commerceId, PeriodGranularity granularity) {
        PeriodRange range = resolveRange(granularity);
        Double avg = averageSaleValueByPeriod(commerceId, range.start(), range.end());
        return avg == null ? BigDecimal.ZERO : BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP);
    }

    default List<PeriodProductCount> getProductsSoldByPeriod(Long commerceId, PeriodGranularity granularity) {
        ZoneId zone = BUSINESS_ZONE;
        PeriodRange range = resolveRange(granularity);
        List<SaleDetail> details = saleDetailsInPeriod(commerceId, range.start(), range.end());

        return switch (granularity) {
            case DAY -> groupByHour(details, zone);
            case WEEK -> groupByDay(details, zone, range.startDate(), range.endDate());
            case MONTH -> groupByWeek(details, zone, range.startDate(), range.endDate());
        };
    }

    private List<PeriodProductCount> groupByHour(List<SaleDetail> details, ZoneId zone) {
        Map<Integer, Long> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getSale().getCreatedAt().atZone(zone).getHour(),
                        Collectors.summingLong(SaleDetail::getQuantity)));

        int currentHour = ZonedDateTime.now(zone).getHour();
        List<PeriodProductCount> result = new ArrayList<>();
        for (int hour = 0; hour <= currentHour; hour++) {
            result.add(new PeriodProductCount(String.format("%02d:00", hour), grouped.getOrDefault(hour, 0L)));
        }
        return result;
    }

    private List<PeriodProductCount> groupByDay(List<SaleDetail> details, ZoneId zone, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Long> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getSale().getCreatedAt().atZone(zone).toLocalDate(),
                        Collectors.summingLong(SaleDetail::getQuantity)));

        List<PeriodProductCount> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            result.add(new PeriodProductCount(date.toString(), grouped.getOrDefault(date, 0L)));
        }
        return result;
    }

    private List<PeriodProductCount> groupByWeek(List<SaleDetail> details, ZoneId zone, LocalDate startDate, LocalDate endDate) {
        Map<Long, Long> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        d -> {
                            LocalDate date = d.getSale().getCreatedAt().atZone(zone).toLocalDate();
                            return ChronoUnit.DAYS.between(startDate, date) / 7;
                        },
                        Collectors.summingLong(SaleDetail::getQuantity)));

        long totalWeeks = ChronoUnit.DAYS.between(startDate, endDate) / 7 + 1;
        List<PeriodProductCount> result = new ArrayList<>();
        for (long week = 0; week < totalWeeks; week++) {
            LocalDate weekStart = startDate.plusDays(week * 7);
            result.add(new PeriodProductCount(weekStart.toString(), grouped.getOrDefault(week, 0L)));
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