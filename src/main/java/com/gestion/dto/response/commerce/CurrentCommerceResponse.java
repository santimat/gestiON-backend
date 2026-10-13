package com.gestion.dto.response.commerce;

public record CurrentCommerceResponse(
        Long commerceId,
        String businessName,
        String businessLogoUrl,
        boolean businessActive,
        Double profitMultiplier
) {
}
