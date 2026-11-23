package com.gestion.service.sale;

import com.gestion.dto.response.sale.PeriodProductCount;
import com.gestion.model.SaleDetail;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class HourlyProductGrouper {

    private final ZoneId zoneId = ZoneId.of("America/Argentina/Buenos_Aires");

    public List<PeriodProductCount> group(List<SaleDetail> details) {
        Map<Integer, Long> grouped = details.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getSale().getCreatedAt().atZone(zoneId).getHour(),
                        Collectors.summingLong(SaleDetail::getQuantity)));

        int currentHour = ZonedDateTime.now(zoneId).getHour();
        List<PeriodProductCount> result = new ArrayList<>();

        for (int hour = 0; hour <= currentHour; hour++) {
            result.add(new PeriodProductCount(String.format("%02d:00", hour), grouped.getOrDefault(hour, 0L)));
        }
        return result;
    }
}