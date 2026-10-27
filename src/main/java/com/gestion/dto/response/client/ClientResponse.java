package com.gestion.dto.response.client;

public record ClientResponse(
        Long id,
        Long commerceId,
        String name,
        String lastName,
        String address,
        String phoneNumber,
        String dni
) {
}
