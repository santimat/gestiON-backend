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
                sale.getSellPoint() != null ? sale.getSellPoint().getName() : null,
                sale.getUser().getName(),
                sale.getPaymentMethod(),
                sale.getSubtotal(),
                sale.getPaymentMethodDiscountType(),
                sale.getPaymentMethodDiscount() != null ? sale.getPaymentMethodDiscount() : BigDecimal.ZERO,
                sale.getTotal(),
                sale.getCreatedAt(),
                sale.getObservations(),
                sale.getStatus(),
                detailResponses
        );
    }

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
                sale.getSubtotal(),
                sale.getPaymentMethodDiscount(),
                sale.getPaymentMethodDiscountType(),
                sale.getTotal()
        );
    }
}
