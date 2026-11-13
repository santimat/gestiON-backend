package com.gestion.dto.response.product;

public record ProductUpdateActiveResponse(
        Long id,
        boolean active
) {
}