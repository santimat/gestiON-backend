package com.gestion.mappers;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.model.Sale;

public class SaleMapper {
    public static Sale toEntity(SaleRequest request) {
        Sale sale = new Sale();

        sale.setPaymentMethod(request.paymentMethod());
        sale.setSubtotal(request.subtotal());
        sale.setDiscount(request.discount());
        sale.setTotal(request.total());
        sale.setCreatedAt(request.createdAt());
        sale.setObservations(request.observations());
        sale.setStatus(request.status());

        return sale;
    }
}
