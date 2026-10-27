package com.gestion.mappers;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.response.sale.SaleDetailResponse;
import com.gestion.dto.response.sale.SaleResponse;
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

        return new SaleResponse(
                sale.getId(),
                sale.getClient() != null ? sale.getClient().getId() : null,
                sale.getSellPoint() != null ? sale.getSellPoint().getId() : null,
                sale.getUser().getName(),
                sale.getPaymentMethod(),
                sale.getSubtotal(),
                sale.getDiscountType(),
                sale.getDiscount(),
                sale.getTotal(),
                sale.getTotalAfterDiscount(),
                sale.getCreatedAt(),
                sale.getObservations(),
                sale.getStatus(),
                detailResponses
        );
    }
}
