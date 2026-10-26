package com.gestion.dto.response.sellPoint;

import java.time.LocalDateTime;
import java.util.List;

public record SellPointResponse(
        Long id,
        Long commerceId,
        String name,
        Boolean active,
        List<Long> cashierIds,
        LocalDateTime createdAt
) {
}
