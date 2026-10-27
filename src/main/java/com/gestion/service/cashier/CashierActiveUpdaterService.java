package com.gestion.service.cashier;

import com.gestion.dto.response.cashier.CashierUpdateActiveResponse;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CashierActiveUpdaterService {
    private final CashierFinderByIdAndCommerceIdService cashierFinderByIdAndCommerceIdService;
    private final JpaUserRepository userRepository;

    @Transactional
    public CashierUpdateActiveResponse toggleActive(Long cashierId, Long commerceId) {
        User cashierToUpdate = cashierFinderByIdAndCommerceIdService
                .findCashierByIdAndCommerceId(cashierId, commerceId);
        cashierToUpdate.setActive(!cashierToUpdate.isActive());
        User updatedCashier = userRepository.save(cashierToUpdate);
        return new CashierUpdateActiveResponse(
                updatedCashier.getId(),
                updatedCashier.isActive()
        );
    }
}
