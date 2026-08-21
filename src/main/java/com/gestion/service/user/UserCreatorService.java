package com.gestion.service.user;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.exception.DuplicateResourceException;
import com.gestion.mappers.UserMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserCreatorService {
    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public User createUser(UserRequest request, Commerce commerce) {
        // existsByEmail retorna un boolean
        if (userRepository.existsByEmail(request.email())) {
            // al tirar la excepción no hace falta un return, ya que el throw corta la ejecución de lo de debajo.
            throw new DuplicateResourceException("User with email " + request.email() + " already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCommerce(commerce);
        return userRepository.save(user);
    }
}

