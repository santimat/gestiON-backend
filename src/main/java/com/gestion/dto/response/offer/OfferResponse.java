package com.gestion.dto.response.offer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OfferResponse(
        Long id,
        Long productId,
        BigDecimal value,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
