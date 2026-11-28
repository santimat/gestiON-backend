package com.gestion.service.user;

import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserSessionsRevokerService {

    private final JpaUserRepository userRepository;

    // el JWT lleva la version del usuario al emitirse (claim "ver"); al incrementarla, JwtFilter
    // rechaza con 401 todos los tokens anteriores
    @Transactional
    public void revokeAllSessions(Long userId) {
        userRepository.incrementVersion(userId);
    }
}
