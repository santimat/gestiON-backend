package com.gestion.mappers;

import com.gestion.dto.request.order.OrderRequest;
import com.gestion.dto.response.order.OrderResponse;
import com.gestion.dto.response.orderDetail.OrderDetailResponse;
import com.gestion.model.Order;

import java.util.List;

public class OrderMapper {

    public static Order toEntity(OrderRequest request) {
        Order order = new Order();
        order.setDescription(request.description());
        return order;
    }

    public static OrderResponse toResponse(Order order, List<OrderDetailResponse> detailResponses) {

        Long commerceId = (order.getCommerce() != null) ? order.getCommerce().getId() : null;
        String businessName = (order.getCommerce() != null) ? order.getCommerce().getBusinessName() : null;

        return new OrderResponse(
                order.getId(),
                commerceId,
                businessName,
                order.getDescription(),
                order.getTotal(),
                detailResponses,
                order.getCreatedAt()
        );
    }
}
