package com.gestion.dto.project;

import java.time.Instant;
import java.time.LocalDate;

public record PeriodRange(Instant start, Instant end, LocalDate startDate, LocalDate endDate) {
}
