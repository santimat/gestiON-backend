package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.model.SaleDetail;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DailyGroupingStrategy implements PeriodGroupingStrategy {
    private final ZoneId zoneId = ZoneId.of("America/Argentina/Buenos_Aires");

    @Override
    public List<PeriodProductCount> group(List<SaleDetail> details, PeriodRange range) {
        Map<LocalDate, Long> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getSale().getCreatedAt().atZone(zoneId).toLocalDate(),
                        Collectors.summingLong(SaleDetail::getQuantity)));

        List<PeriodProductCount> result = new ArrayList<>();
        // rellenar con 0 los días sin ventas
        for (LocalDate date = range.startDate(); !date.isAfter(range.endDate()); date = date.plusDays(1)) {
            result.add(new PeriodProductCount(date.toString(), grouped.getOrDefault(date, 0L)));
        }
        return result;
    }
}