package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Sale;
import com.gestion.model.User;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SaleEntityBuilderService {
    private final EntityManager entityManager;

    public Sale build(SaleRequest request, Long userId, Long commerceId, BigDecimal subtotal) {
        Sale sale = SaleMapper.toEntity(request);
        sale.setUser(entityManager.getReference(User.class, userId));
        sale.setCommerce(entityManager.getReference(Commerce.class, commerceId));
        sale.setSubtotal(subtotal);
        return sale;
    }
}