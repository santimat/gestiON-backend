package com.gestion.service.cashier;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.cashier.CashierUpdateRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.mappers.CashierMapper;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CashierUpdaterService {
    private final JpaUserRepository userRepository;
    private final CashierFinderByIdAndCommerceIdService cashierFinderByIdAndCommerceIdService;
    private final CashierValidatorByEmailService cashierValidatorByEmailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse updateCashier(Long cashierId, CashierUpdateRequest request, UserPrincipal authenticatedUser) {
        if (authenticatedUser.getCommerceId() == null) {
            throw new AccessDeniedException("Authenticated user does not have a commerce associated");
        }

        User cashier = cashierFinderByIdAndCommerceIdService.findCashierByIdAndCommerceId(
                cashierId, authenticatedUser.getCommerceId());
        cashierValidatorByEmailService.checkExistingCashierByEmail(cashier.getId(), request.email());

        cashier.setName(request.name());
        cashier.setEmail(request.email());
        cashier.setPhoneNumber(request.phoneNumber());
        if (request.password() != null && !request.password().isBlank()) {
            cashier.setPassword(passwordEncoder.encode(request.password()));
        }

        User savedCashier = userRepository.save(cashier);
        return CashierMapper.toResponse(savedCashier);
    }
}
