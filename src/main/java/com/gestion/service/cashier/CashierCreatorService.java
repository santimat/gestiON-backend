package com.gestion.service.cashier;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.cashier.CashierRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.enums.Role;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.mappers.CashierMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CashierCreatorService {
    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CommerceFinderByIdService commerceFinderByIdService;

    @Transactional
    public UserResponse createCashier(CashierRequest request, UserPrincipal authenticatedUser) {
        if (authenticatedUser == null || authenticatedUser.getCommerceId() == null) {
            throw new AccessDeniedException("Authenticated user or commerce ID is null");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(ErrorCode.EMAIL_ALREADY_EXISTS, "User with email " + request.email() + " already exists");
        }

        Commerce commerce = commerceFinderByIdService.findCommerceById(authenticatedUser.getCommerceId());

        User cashier = CashierMapper.toEntity(request);
        cashier.setPassword(passwordEncoder.encode(cashier.getPassword()));
        cashier.setCommerce(commerce);
        cashier.setRole(Role.CASHIER);
        cashier.setActive(true);

        User savedCashier = userRepository.save(cashier);
        return CashierMapper.toResponse(savedCashier);
    }
}
