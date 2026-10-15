package com.gestion.mappers;

import com.gestion.dto.request.orderDetail.OrderDetailRequest;
import com.gestion.dto.response.orderDetail.OrderDetailResponse;
import com.gestion.model.Order;
import com.gestion.model.OrderDetail;
import com.gestion.model.Product;

public class OrderDetailMapper {
    public static OrderDetailResponse toDetailResponse(OrderDetail detail) {
        return new OrderDetailResponse(
                detail.getId(),
                detail.getOrder().getId(),
                detail.getProduct().getId(),
                detail.getProduct().getName(),
                detail.getUnitPrice(),
                detail.getQuantity(),
                detail.getSubTotal()
        );
    } // TODO: preguntar si esta bien

    public static OrderDetail toEntity(OrderDetailRequest request, Product product, Order order) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setProduct(product);
        orderDetail.setQuantity(request.quantity());
        orderDetail.setUnitPrice(product.getSalePrice());

        return orderDetail;
    }

}
