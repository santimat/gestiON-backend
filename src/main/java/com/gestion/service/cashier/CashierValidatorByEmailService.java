package com.gestion.service.cashier;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CashierValidatorByEmailService {
    private final JpaUserRepository userRepository;

    public void checkExistingCashierByEmail(Long userIdToExclude, String email) {
        if (userRepository.existsByEmailAndIdNot(email, userIdToExclude)) {
            throw new DuplicateResourceException(ErrorCode.EMAIL_ALREADY_EXISTS,
                    "User with email " + email + " already exists");
        }
    }
}
