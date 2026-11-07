package com.gestion.dto.response.category;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        Long commerceId,
        String name,
        String description,
        boolean active,
        Instant updatedAt,
        long productCount
) {
}
