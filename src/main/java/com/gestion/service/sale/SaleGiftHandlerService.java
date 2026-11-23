package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.enums.Role;
import com.gestion.enums.SaleStatus;
import com.gestion.exception.GiftOnlyForOwnerException;
import com.gestion.exception.ObservationRequiredForGiftException;
import com.gestion.model.Sale;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SaleGiftHandlerService {

    public void handleIfGift(SaleRequest request, Sale sale, Role role) {
        boolean isGift = Boolean.TRUE.equals(request.gift());

        if (!isGift) {
            return;
        }

        // solo OWNER puede marcar como gift
        if (role != Role.OWNER) {
            throw new GiftOnlyForOwnerException();
        }
        // gift requiere observación
        if (request.observations() == null || request.observations().isBlank()) {
            throw new ObservationRequiredForGiftException();
        }
        // gift: status GIFTED, subtotal = precio real, total = 0, sin descuentos
        sale.setStatus(SaleStatus.GIFTED);
        sale.setTotal(BigDecimal.ZERO);
    }
}