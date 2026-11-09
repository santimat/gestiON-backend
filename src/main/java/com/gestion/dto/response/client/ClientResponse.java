package com.gestion.dto.response.client;

import java.math.BigDecimal;

public record ClientResponse(
        Long id,
        Long commerceId,
        String name,
        String lastName,
        String address,
        String phoneNumber,
        String dni,
        Long salesCount,
        Long pendingSalesCount,
        BigDecimal totalSpent,
        BigDecimal totalPending
) {
}