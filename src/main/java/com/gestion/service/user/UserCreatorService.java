package com.gestion.service.user;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.enums.Role;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.mappers.UserMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserCreatorService {
    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserRequest request, Role userRole, Commerce commerce) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User with email " + request.email() + " already exists");
        }

        User user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCommerce(commerce);

        user.setRole(userRole);
        user.setActive(true);
        return userRepository.save(user);
    }
}

