package com.gestion.service.user;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.UserTokenResponse;
import com.gestion.enums.Role;
import com.gestion.enums.UserStatus;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.mappers.UserMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import com.gestion.service.commerce.CommerceFinderByIdService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserCreatorService {
    private final JpaUserRepository userRepository;
    private final CommerceFinderByIdService commerceFinderByIdService;
    private final PasswordEncoder passwordEncoder;

    public UserTokenResponse createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User with email " + request.email() + " already exists");
        }

        Commerce commerce = commerceFinderByIdService.findById(request.commerceId());

        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCommerce(commerce);
        // TODO: preguntar manejo del rol
        // opciones, crear dos endpoints (un para crear el usuario owner y otro usuario cashier), por ahi tiene
        // sentido ya que requieren roles distintos.
        user.setRole(Role.OWNER);
        user.setStatus(UserStatus.ACTIVE);
        User newUser = userRepository.save(user);
        return UserMapper.toTokenResponse(newUser);
    }
}

