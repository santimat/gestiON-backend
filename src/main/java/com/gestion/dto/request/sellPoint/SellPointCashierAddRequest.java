package com.gestion.dto.request.sellPoint;

import jakarta.validation.constraints.NotNull;

public record SellPointCashierAddRequest(
        @NotNull(message = "Cashier id is required")
        Long cashierId
) {
}
