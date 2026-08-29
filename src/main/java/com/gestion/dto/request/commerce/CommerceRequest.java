package com.gestion.dto.request.commerce;


public record CommerceRequest(
        String businessName,
        String address,
        String phoneNumber,
        String cuit
) {
}
