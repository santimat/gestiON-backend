package com.gestion.dto.response.commerce;

import java.time.Instant;

public record CommerceWithOwnerResponse(
        Long userId,
        String username,
        String email,
        String phoneNumber,
        Long commerceId,
        String name,
        String cuit,
        String address,
        String businessLogoUrl,
        boolean businessActive,
        Double profitMultiplier,
        Instant updatedAt
) {
}
