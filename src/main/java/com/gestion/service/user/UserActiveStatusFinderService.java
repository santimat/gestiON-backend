package com.gestion.service.user;

import com.gestion.dto.project.UserActiveStatus;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserActiveStatusFinderService {

    private final JpaUserRepository userRepository;

    /**
     * Revalida el estado del usuario (y de su comercio) contra la base.
     *
     * El JWT lleva 24 horas de vida y el flag active del usuario no se mira despues del login,
     * asi que un usuario desactivado conservaba acceso hasta que expiraba su token.
     */
    @Transactional(readOnly = true)
    public UserActiveStatus findActiveStatusByUserId(Long userId) {
        return userRepository.findActiveStatusById(userId).orElse(null);
    }
}
