package com.gestion.service.user;

import com.gestion.exception.DuplicateResourceException;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserValidatorByEmailService {
    private final JpaUserRepository userRepository;

    public void checkExistingUserByEmail(String email) {
        if (userRepository.existsByEmail(email))
            throw new DuplicateResourceException("User with email " + email + " already exists");
    }
}
