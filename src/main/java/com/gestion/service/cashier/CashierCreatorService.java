package com.gestion.service.cashier;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.cashier.CashierRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.enums.Role;
import com.gestion.mappers.CashierMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import com.gestion.service.user.UserValidatorByEmailService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CashierCreatorService {
    private final JpaUserRepository userRepository;
    private final UserValidatorByEmailService userValidatorByEmailService;
    private final PasswordEncoder passwordEncoder;
    private final CommerceFinderByIdService commerceFinderByIdService;

    @Transactional
    public UserResponse createCashier(CashierRequest request, UserPrincipal authenticatedUser) {
        if (authenticatedUser.getCommerceId() == null) {
            throw new AccessDeniedException("Authenticated user does not have a commerce associated");
        }

        userValidatorByEmailService.checkExistingUserByEmail(request.email());

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
