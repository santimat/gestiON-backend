package com.gestion.dto.response.commerce;

public record CommerceUpdateActiveResponse(
        Long commerceId,
        Boolean active
) {
}
