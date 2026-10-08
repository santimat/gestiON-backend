package com.gestion.service.order;

import com.gestion.dto.request.order.OrderRequest;
import com.gestion.dto.response.order.OrderResponse;
import com.gestion.dto.response.orderDetail.OrderDetailResponse;
import com.gestion.mappers.OrderDetailMapper;
import com.gestion.mappers.OrderMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Order;
import com.gestion.model.OrderDetail;
import com.gestion.model.Product;
import com.gestion.repository.JpaOrderDetailRepository;
import com.gestion.repository.JpaOrderRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import com.gestion.service.product.ProductFinderByIdService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderCreatorService {
    private final JpaOrderRepository jpaOrderRepository;
    private final JpaOrderDetailRepository jpaOrderDetailRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final ProductFinderByIdService productFinderByIdService;

    public OrderResponse createOrder(OrderRequest request, Long commerceId) {

        Commerce commerce = commerceFinderByIdService.findCommerceById(commerceId);
        Order order = OrderMapper.toEntity(request);
        order.setCommerce(commerce);

        BigDecimal totalAccumulated = BigDecimal.ZERO;

        List<OrderDetail> details = request.details().stream().map((detail -> {
            Product product = productFinderByIdService.findProductById(detail.productId());

            BigDecimal subTotal = detail.unitPrice()
                    .multiply(BigDecimal.valueOf(detail.quantity()));

            totalAccumulated.add(subTotal);

            return OrderDetailMapper.toEntity(detail, product);
        })).toList();

        order.setTotal(totalAccumulated);

        Order newOrder = jpaOrderRepository.save(order);
        List<OrderDetail> savedOrderDetails = jpaOrderDetailRepository.saveAllAndFlush(details);
        List<OrderDetailResponse> orderDetailResponses = savedOrderDetails.stream().map(OrderDetailMapper::toDetailResponse).toList();
        return OrderMapper.toResponse(order, orderDetailResponses);
    }

}
