package com.gestion.dto.response.sellpoint;

import java.time.Instant;

public record SellPointResponse(
        Long id,
        Long commerceId,
        String name,
        boolean active,
        Instant updatedAt
) {
}
