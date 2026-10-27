package com.gestion.dto.response.commerce;

public record CommerceUpdateActiveResponse(
        Long commerceId,
        boolean active
) {
}
