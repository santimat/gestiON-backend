package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.enums.PeriodGranularity;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductsSoldByPeriodService {

    private final JpaSaleRepository jpaSaleRepository;
    private final PeriodRangeService periodRangeService;
    private final ZoneId zoneId = ZoneId.of("America/Argentina/Buenos_Aires");

    public List<PeriodProductCount> getProductsSoldByPeriod(Long commerceId, PeriodGranularity granularity) {
        PeriodRange range = periodRangeService.resolveRange(granularity);
        List<SaleDetail> details = jpaSaleRepository.saleDetailsInPeriod(
                commerceId, range.start(), range.end());

        return switch (granularity) {
            case DAY -> groupByHour(details);
            case WEEK -> groupByDay(details, range.startDate(), range.endDate());
            case MONTH -> groupByWeek(details, range.startDate(), range.endDate());
        };
    }

    private List<PeriodProductCount> groupByHour(List<SaleDetail> details) {
        Map<Integer, Long> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getSale().getCreatedAt().atZone(zoneId).getHour(),
                        Collectors.summingLong(SaleDetail::getQuantity)));

        int currentHour = ZonedDateTime.now(zoneId).getHour();
        List<PeriodProductCount> result = new ArrayList<>();

        // rellenar con 0 las horas sin ventas
        for (int hour = 0; hour <= currentHour; hour++) {
            result.add(new PeriodProductCount(String.format("%02d:00", hour), grouped.getOrDefault(hour, 0L)));
        }
        return result;
    }

    private List<PeriodProductCount> groupByDay(List<SaleDetail> details, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Long> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getSale().getCreatedAt().atZone(zoneId).toLocalDate(),
                        Collectors.summingLong(SaleDetail::getQuantity)));

        List<PeriodProductCount> result = new ArrayList<>();
        // rellenar con 0 los días sin ventas
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            result.add(new PeriodProductCount(date.toString(), grouped.getOrDefault(date, 0L)));
        }
        return result;
    }

    private List<PeriodProductCount> groupByWeek(List<SaleDetail> details, LocalDate startDate, LocalDate endDate) {
        Map<Long, Long> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        d -> {
                            LocalDate date = d.getSale().getCreatedAt().atZone(zoneId).toLocalDate();
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
}