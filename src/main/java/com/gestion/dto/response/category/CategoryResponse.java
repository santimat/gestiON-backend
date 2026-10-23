package com.gestion.dto.response.category;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        Long commerceId,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt
) {
}
