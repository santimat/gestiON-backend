package com.gestion.dto.response.sale;

import com.gestion.enums.SaleStatus;

public record SaleUpdateStatusResponse(
        Long id,
        SaleStatus status
) {
}
