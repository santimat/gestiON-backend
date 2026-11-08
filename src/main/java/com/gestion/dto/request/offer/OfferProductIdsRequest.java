package com.gestion.dto.request.offer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OfferProductIdsRequest(
        @NotNull(message = "Product ids are required")
        @NotEmpty(message = "Product ids cannot be empty")
        List<Long> productIds
) {
}