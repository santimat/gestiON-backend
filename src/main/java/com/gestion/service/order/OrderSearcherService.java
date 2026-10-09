package com.gestion.service.order;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.order.OrderResponse;
import com.gestion.dto.response.orderDetail.OrderDetailResponse;
import com.gestion.mappers.OrderDetailMapper;
import com.gestion.mappers.OrderMapper;
import com.gestion.model.Order;
import com.gestion.model.OrderDetail;
import com.gestion.repository.JpaOrderDetailRepository;
import com.gestion.repository.JpaOrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderSearcherService {
    private final JpaOrderRepository jpaOrderRepository;
    private final JpaOrderDetailRepository jpaOrderDetailRepository;

    public Page<OrderResponse> searchOrders(UserPrincipal authenticatedUser, Pageable pageable) {

        Page<Order> orders = jpaOrderRepository.findAllByCommerceId(authenticatedUser.getCommerceId());

        if (orders.isEmpty()) {
            return orders.map(order -> OrderMapper.toResponse(order, Collections.emptyList()));
        }

        // 2. Extraemos los IDs de las órdenes de esta página
        List<Long> orderIds = orders.getContent().stream()
                .map(Order::getId)
                .toList();

        // 3. Traemos TODOS los detalles de estas órdenes en una sola consulta SQL
        List<OrderDetail> details = jpaOrderDetailRepository.findByOrderIdIn(orderIds);

        // 4. Mapeamos y agrupamos los detalles por orderId en un Map
        Map<Long, List<OrderDetailResponse>> detailsByOrderId = details.stream()
                .collect(Collectors.groupingBy(
                        detail -> detail.getOrder().getId(),
                        Collectors.mapping(OrderDetailMapper::toDetailResponse, Collectors.toList())
                ));

        // 5. Mapeamos la página usando toResponse existente de 2 parámetros
        return orders.map(order -> {
            List<OrderDetailResponse> orderDetails = detailsByOrderId.getOrDefault(order.getId(), Collections.emptyList());
            return OrderMapper.toResponse(order, orderDetails);
        });
    }
}

