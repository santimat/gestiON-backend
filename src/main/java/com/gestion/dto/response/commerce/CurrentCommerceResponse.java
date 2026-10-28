package com.gestion.dto.response.commerce;

public record CurrentCommerceResponse(
        Long commerceId,
        String name,
        String businessLogoUrl,
        boolean businessActive,
        Double profitMultiplier
) {
}
