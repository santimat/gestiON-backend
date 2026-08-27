package com.gestion.dto.request.sale;

public record SaleRequest(

        List <SaleDetailRequest> saleDetails,
) {
}
