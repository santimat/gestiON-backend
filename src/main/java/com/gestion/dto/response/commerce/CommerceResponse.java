package com.gestion.dto.response.commerce;

public record CommerceResponse(
        Long id,
        String businessName,
        String address,
        String phoneNumber,
        String cuit,
        String logoUrl
) {
}
