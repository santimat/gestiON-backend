package com.gestion.dto.request.sellpoint;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record SellPointRequest(
        @NotBlank(message = "Name is required")
        @Length(max = 100, message = "Sell point name's length must be 100 characters or fewer")
        String name
) {
}
