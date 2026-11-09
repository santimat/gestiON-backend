package com.gestion.dto.request.offer;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OfferProductIdsRequest(
        @NotNull(message = "Product ids are required")
        List<Long> productIds
) {
}