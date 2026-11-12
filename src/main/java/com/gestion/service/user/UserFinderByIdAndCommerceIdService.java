package com.gestion.service.user;

import com.gestion.enums.ErrorCode;
import com.gestion.enums.Role;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resuelve un usuario por id exigiendo que pertenezca al comercio indicado.
 *
 * Un finder por id sin scope permite tocar un usuario de otro comercio: el id viene del path y
 * no de un token, asi que hay que verificar que el usuario pertenezca al comercio del path antes
 * de modificarlo.
 */
@Service
@AllArgsConstructor
public class UserFinderByIdAndCommerceIdService {

    private final JpaUserRepository userRepository;

    public User findUserByIdAndCommerceIdAndRole(Long id, Long commerceId, Role role) {
        return userRepository.findByIdAndCommerceIdAndRole(id, commerceId, role)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User with id " + id + " not found"));
    }
}
