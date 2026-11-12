package com.gestion.service.user;

import com.gestion.enums.ErrorCode;
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
            throw new DuplicateResourceException(ErrorCode.EMAIL_ALREADY_EXISTS, "User with email " + email + " already exists");
    }

    /**
     * Al actualizar, el propio usuario ya tiene ese email: el chequeo tiene que excluirlo, si no
     * el usuario no podria guardar sus propios datos sin cambiar el email.
     */
    public void checkExistingUserByEmailAndIdNot(String email, Long id) {
        if (userRepository.existsByEmailAndIdNot(email, id))
            throw new DuplicateResourceException(ErrorCode.EMAIL_ALREADY_EXISTS, "User with email " + email + " already exists");
    }
}
