package com.gestion.service.user;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.UserTokenResponse;
import com.gestion.enums.Role;
import com.gestion.enums.UserStatus;
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
    private final UserValidatorByEmailService userValidatorByEmailService;

    public UserTokenResponse createUser(UserRequest request, UserPrincipal authenticatedUser) {
        userValidatorByEmailService.checkExistingUserByEmail(request.email());

        Commerce commerce = commerceFinderByIdService.findById(request.commerceId());

        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCommerce(commerce);

        Role authenticatedUserRole = authenticatedUser.getRole();
        user.setRole(Role.SUDO.equals(authenticatedUserRole) ? Role.OWNER : Role.CASHIER);
        
        user.setStatus(UserStatus.ACTIVE);
        User newUser = userRepository.save(user);
        return UserMapper.toTokenResponse(newUser);
    }
}

