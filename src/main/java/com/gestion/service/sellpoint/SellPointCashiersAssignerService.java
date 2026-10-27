package com.gestion.service.sellpoint;

import com.gestion.enums.ErrorCode;
import com.gestion.enums.Role;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.SellPoint;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class SellPointCashiersAssignerService {
    private final JpaUserRepository userRepository;

    @Transactional
    public void assignCashiers(Set<Long> cashierIds, Long commerceId, SellPoint sellPoint) {
        if (cashierIds == null || cashierIds.isEmpty()) {
            return;
        }

        for (Long cashierId : cashierIds) {
            User cashier = userRepository.findByIdAndCommerceIdAndRole(cashierId, commerceId, Role.CASHIER)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND,
                            "Cashier with id " + cashierId + " not found in this commerce"));
            cashier.setSellPoint(sellPoint);
        }
    }
}
