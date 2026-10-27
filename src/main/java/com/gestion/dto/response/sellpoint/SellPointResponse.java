package com.gestion.dto.response.sellpoint;

import java.time.LocalDateTime;

public record SellPointResponse(
        Long id,
        Long commerceId,
        String name,
        boolean active,
        LocalDateTime updatedAt
) {
}
