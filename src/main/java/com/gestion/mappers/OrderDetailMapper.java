package com.gestion.mappers;

import com.gestion.dto.request.orderDetail.OrderDetailRequest;
import com.gestion.dto.response.orderDetail.OrderDetailResponse;
import com.gestion.model.OrderDetail;
import com.gestion.model.Product;

public class OrderDetailMapper {
    public static OrderDetailResponse toDetailResponse(OrderDetail detail) {

        return new OrderDetailResponse(
                detail.getId(),
                detail.getProduct(),
                detail.getUnitPrice(),
                detail.getQuantity(),
                detail.getSubTotal()
        );
    }


    public static OrderDetail toEntity(OrderDetailRequest request, Product product) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setProduct(product);
        orderDetail.setQuantity(request.quantity());
        orderDetail.setUnitPrice(request.unitPrice());

        return orderDetail;
    }

}
