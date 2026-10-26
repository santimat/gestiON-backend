package com.gestion.service.sellPoint;

import com.gestion.config.UserPrincipal;
import com.gestion.model.SellPoint;
import com.gestion.model.User;
import com.gestion.repository.JpaSellPointRepository;
import com.gestion.service.cashier.CashierFinderByIdAndCommerceIdService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SellPointCashierRemoverService {
    private final JpaSellPointRepository sellPointRepository;
    private final SellPointFinderByIdAndCommerceIdService sellPointFinderByIdAndCommerceIdService;
    private final CashierFinderByIdAndCommerceIdService cashierFinderByIdAndCommerceIdService;

    @Transactional
    public void removeCashier(Long sellPointId, Long cashierId, UserPrincipal authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.getCommerceId() == null) {
            throw new AccessDeniedException("Authenticated user or commerce ID is null");
        }

        SellPoint sellPoint = sellPointFinderByIdAndCommerceIdService
                .findSellPointByIdAndCommerceId(sellPointId, authenticatedUser.getCommerceId());

        // valida que sea un cajero del propio comercio antes de tocar la colección
        User cashier = cashierFinderByIdAndCommerceIdService
                .findCashierByIdAndCommerceId(cashierId, authenticatedUser.getCommerceId());

        sellPoint.getCashiers().remove(cashier);
        sellPointRepository.save(sellPoint);
    }
}
