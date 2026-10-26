package com.gestion.service.cashier;

import com.gestion.enums.ErrorCode;
import com.gestion.enums.Role;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CashierFinderByIdAndCommerceIdService {
    private final JpaUserRepository userRepository;

    @Transactional(readOnly = true)
    public User findCashierByIdAndCommerceId(Long id, Long commerceId) {
        return userRepository.findByIdAndCommerceIdAndRole(id, commerceId, Role.CASHIER)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, "Cashier with id " + id + " not found"));
    }
}
