package com.gestion.dto.response.commerce;

public record CommerceResponse(
        Long id,
        String businessName,
        String address,
        String cuit,
        String logoUrl
) {
}
