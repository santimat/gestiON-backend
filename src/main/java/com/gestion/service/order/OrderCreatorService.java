package com.gestion.service.order;

import com.gestion.dto.request.order.OrderRequest;
import com.gestion.dto.request.orderDetail.OrderDetailRequest;
import com.gestion.mappers.OrderMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Order;
import com.gestion.model.OrderDetail;
import com.gestion.model.Product;
import com.gestion.repository.JpaOrderRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class OrderCreatorService {
    private final JpaOrderRepository jpaOrderRepository;
    private final EntityManager entityManager;

    public Order createOrder(OrderRequest request, Long commerceId) {
        Order order = OrderMapper.toEntity(request);

        Commerce commerceProxy = entityManager.getReference(Commerce.class, commerceId);
        order.setCommerce(commerceProxy);

        BigDecimal totalAccumulated = BigDecimal.ZERO;

        for (OrderDetailRequest detailReq : request.details()) {
            Product productProxy = entityManager.getReference(Product.class, detailReq.productId());

            BigDecimal subTotal = detailReq.unitPrice()
                    .multiply(BigDecimal.valueOf(detailReq.quantity()));

            OrderDetail detail = new OrderDetail();
            detail.setProduct(productProxy);
            detail.setUnitPrice(detailReq.unitPrice());
            detail.setQuantity(detailReq.quantity());
            detail.setSubTotal(subTotal);

            order.addDetail(detail);

            totalAccumulated = totalAccumulated.add(subTotal);
        }

        order.setTotal(totalAccumulated);

        return jpaOrderRepository.save(order);

    }

}
