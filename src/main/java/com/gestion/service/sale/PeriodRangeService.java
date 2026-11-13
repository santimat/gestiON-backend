package com.gestion.service.sale;

import com.gestion.dto.project.PeriodRange;
import com.gestion.enums.PeriodGranularity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@AllArgsConstructor
public class PeriodRangeService {

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    public PeriodRange resolveRange(PeriodGranularity granularity) {
        Instant now = Instant.now();
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate startDate;

        switch (granularity) {
            case DAY -> startDate = today;
            case WEEK -> startDate = today.minusDays(6);
            case MONTH -> startDate = today.minusDays(29);
            default -> throw new IllegalArgumentException("Unsupported granularity: " + granularity);
        }

        Instant start = startDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        return new PeriodRange(start, now, startDate, today);
    }
}