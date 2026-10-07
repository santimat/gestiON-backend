package com.gestion.mappers;

import com.gestion.dto.request.order.OrderRequest;
import com.gestion.dto.response.order.OrderResponse;
import com.gestion.dto.response.orderDetail.OrderDetailResponse;
import com.gestion.model.Order;
import com.gestion.model.OrderDetail;

import java.util.List;

public class OrderMapper {

    public static Order toEntity(OrderRequest request) {
        Order order = new Order();
        order.setDescription(request.description());
        return order;
    }

    public static OrderResponse toResponse(Order order) {

        //Aca transforma en lista cada uno de los detalles
        List<OrderDetailResponse> detailResponses = (order.getDetails() != null)
                ? order.getDetails().stream()
                .map(OrderMapper::toDetailResponse)
                .toList()
                : List.of();

        Long commerceId = (order.getCommerce() != null) ? order.getCommerce().getId() : null;
        String businessName = (order.getCommerce() != null) ? order.getCommerce().getBusinessName() : null;

        return new OrderResponse(
                order.getId(),
                commerceId,
                businessName,
                order.getDescription(),
                order.getTotal(),
                detailResponses, // <-- ya no debería marcar error
                order.getCreatedAt()
        );
    }

    // esto pasa el detail a detail response(1)
    private static OrderDetailResponse toDetailResponse(OrderDetail detail) {
        Long productId = (detail.getProduct() != null) ? detail.getProduct().getId() : null;

        return new OrderDetailResponse(
                detail.getId(),
                productId,
                detail.getUnitPrice(),
                detail.getQuantity(),
                detail.getSubTotal()
        );
    }
}
