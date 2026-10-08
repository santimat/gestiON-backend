package com.gestion.dto.response.commerce;

import java.time.LocalDateTime;

public record CommerceWithOwnerResponse(
        Long userId,
        String username,
        String email,
        String phoneNumber,
        Long commerceId,
        String businessName,
        String cuit,
        String address,
        String businessLogoUrl,
        boolean businessActive,
        Double profitMultiplier,
        LocalDateTime updatedAt
) {
}
