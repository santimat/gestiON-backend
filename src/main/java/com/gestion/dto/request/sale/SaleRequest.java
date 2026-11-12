package com.gestion.dto.request.sale;

import com.gestion.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record SaleRequest(
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotEmpty(message = "At least one detail is required")
        @Valid
        Set<SaleDetailRequest> details,

        Long clientId,

        @Length(max = 200, message = "Observations's length must be 200 characters or fewer")
        String observations
) {
}
