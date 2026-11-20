package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.model.SaleDetail;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WeeklyGroupingStrategy implements PeriodGroupingStrategy {
    private final ZoneId zoneId = ZoneId.of("America/Argentina/Buenos_Aires");

    @Override
    public List<PeriodProductCount> group(List<SaleDetail> details, PeriodRange range) {
        Map<Long, Long> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        d -> {
                            LocalDate date = d.getSale().getCreatedAt().atZone(zoneId).toLocalDate();
                            return ChronoUnit.DAYS.between(range.startDate(), date) / 7;
                        },
                        Collectors.summingLong(SaleDetail::getQuantity)));

        long totalWeeks = ChronoUnit.DAYS.between(range.startDate(), range.endDate()) / 7 + 1;
        List<PeriodProductCount> result = new ArrayList<>();
        for (long week = 0; week < totalWeeks; week++) {
            LocalDate weekStart = range.startDate().plusDays(week * 7);
            result.add(new PeriodProductCount(weekStart.toString(), grouped.getOrDefault(week, 0L)));
        }
        return result;
    }
}