package com.gestion.dto.response.category;

public record CategoryResponse(
        Long id,
        Long commerceId,
        String name,
        String description,
        boolean active
) {
}
