package com.gestion.dto.request.order;

import com.gestion.dto.request.orderDetail.OrderDetailRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(
        String description,

        @NotEmpty(message = "The order must contain at least one detail.")
        @Valid // valida cada elemento adentro de la lista
        List<OrderDetailRequest> details
) {
}
