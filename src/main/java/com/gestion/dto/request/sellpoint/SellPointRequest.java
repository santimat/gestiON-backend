package com.gestion.dto.request.sellpoint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record SellPointRequest(
        @NotBlank(message = "Name is required")
        @Length(max = 100, message = "Sell point name's length must be 100 characters or fewer")
        String name,

        @Size(max = 100, message = "Cannot assign more than 100 cashiers at once")
        Set<@NotNull(message = "Cashier id cannot be null") Long> cashierIds
) {
}
