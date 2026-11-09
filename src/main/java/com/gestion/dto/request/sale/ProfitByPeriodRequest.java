package com.gestion.dto.request.sale;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProfitByPeriodRequest(
        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate
) {
}