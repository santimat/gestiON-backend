package com.gestion.dto.response.commerce;

import jakarta.annotation.Nullable;

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
        @Nullable
        String businessLogoUrl,
        boolean businessActive,
        LocalDateTime createdAt
) {
}
