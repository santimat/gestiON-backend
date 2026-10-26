package com.gestion.dto.request.sellPoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SellPointRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name's length must be between 2 and 50")
        String name
) {
}
