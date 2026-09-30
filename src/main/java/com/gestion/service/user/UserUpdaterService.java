package com.gestion.service.user;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserUpdaterService {
    private final JpaUserRepository userRepository;
    private final UserFinderByIdService finderByIdService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User updateUser(UserRequest request, Long userId) {
        User userToUpdate = finderByIdService.findById(userId);

        userToUpdate.setName(request.name());
        userToUpdate.setEmail(request.email());
        userToUpdate.setPhoneNumber(request.phoneNumber());
        // TODO: de momento la contraseña cambia el admin SUDO. lo mejor sería que se envíe un codigo al email correspondiente para mayor seguridad
        userToUpdate.setPassword(passwordEncoder.encode(request.password()));

        return userRepository.save(userToUpdate);
    }
}
