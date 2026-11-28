package com.gestion.dto.request.offer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OfferProductIdsRequest(
        @NotNull(message = "Product ids are required")
        @Size(max = 500, message = "Cannot assign more than 500 products at once")
        List<@NotNull(message = "Product id cannot be null") Long> productIds
) {
}