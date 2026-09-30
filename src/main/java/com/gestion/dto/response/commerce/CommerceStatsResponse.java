package com.gestion.dto.response.commerce;

public record CommerceStatsResponse(
        Long total,
        Long active,
        Long inactive
) {
}
