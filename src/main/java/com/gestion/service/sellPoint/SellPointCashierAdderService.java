package com.gestion.service.sellPoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sellPoint.SellPointResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.InactiveResourceException;
import com.gestion.mappers.SellPointMapper;
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
public class SellPointCashierAdderService {
    private final JpaSellPointRepository sellPointRepository;
    private final SellPointFinderByIdAndCommerceIdService sellPointFinderByIdAndCommerceIdService;
    private final CashierFinderByIdAndCommerceIdService cashierFinderByIdAndCommerceIdService;

    @Transactional
    public SellPointResponse addCashier(Long sellPointId, Long cashierId, UserPrincipal authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.getCommerceId() == null) {
            throw new AccessDeniedException("Authenticated user or commerce ID is null");
        }

        SellPoint sellPoint = sellPointFinderByIdAndCommerceIdService
                .findSellPointByIdAndCommerceId(sellPointId, authenticatedUser.getCommerceId());

        // garantiza existencia + role = CASHIER + mismo comercio; un OWNER u otro user no matchea
        User cashier = cashierFinderByIdAndCommerceIdService
                .findCashierByIdAndCommerceId(cashierId, authenticatedUser.getCommerceId());

        if (!Boolean.TRUE.equals(cashier.getActive())) {
            throw new InactiveResourceException(ErrorCode.ACCOUNT_DISABLED,
                    "Cashier with id " + cashierId + " is inactive");
        }

        sellPoint.getCashiers().add(cashier);
        SellPoint updatedSellPoint = sellPointRepository.save(sellPoint);
        return SellPointMapper.toResponse(updatedSellPoint);
    }
}
