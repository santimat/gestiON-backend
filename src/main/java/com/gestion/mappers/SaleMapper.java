package com.gestion.mappers;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.response.sale.SaleDetailResponse;
import com.gestion.dto.response.sale.SaleResponse;
import com.gestion.dto.response.sale.SaleSummaryResponse;
import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class SaleMapper {

    public static Sale toEntity(SaleRequest request) {
        Sale sale = new Sale();

        sale.setPaymentMethod(request.paymentMethod());
        sale.setObservations(request.observations());
        sale.setDiscountType(request.discountType());
        sale.setDiscount(request.discount() != null ? request.discount() : BigDecimal.ZERO);
        return sale;
    }

    public static SaleResponse toResponse(Sale sale, List<SaleDetail> details) {
        List<SaleDetailResponse> detailResponses = details.stream()
                .map(SaleDetailMapper::toResponse)
                .collect(Collectors.toList());

        String clientName = sale.getClient() != null
                ? sale.getClient().getName() + " " + sale.getClient().getLastName()
                : "Consumidor final";

        return new SaleResponse(
                sale.getId(),
                sale.getClient() != null ? sale.getClient().getId() : null,
                clientName,
                sale.getSellPoint() != null ? sale.getSellPoint().getId() : null,
                sale.getUser().getName(),
                sale.getPaymentMethod(),
                sale.getSubtotal(),
                sale.getDiscountType(),
                sale.getDiscount(),
                sale.getTotal(),
                sale.getCreatedAt(),
                sale.getObservations(),
                sale.getStatus(),
                detailResponses
        );
    }

    /** Versión resumida para el listado del front: "Consumidor final" si la venta no tiene cliente. */
    public static SaleSummaryResponse toSummaryResponse(Sale sale, Integer itemCount) {
        String clientName = sale.getClient() != null
                ? sale.getClient().getName() + " " + sale.getClient().getLastName()
                : "Consumidor final";
        return new SaleSummaryResponse(
                sale.getId(),
                itemCount,
                sale.getStatus(),
                clientName,
                sale.getPaymentMethod(),
                sale.getCreatedAt(),
                sale.getTotal()
        );
    }
}
