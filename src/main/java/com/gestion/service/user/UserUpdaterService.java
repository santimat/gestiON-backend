package com.gestion.service.user;

import com.gestion.dto.request.user.UserUpdateRequest;
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
    public User updateUser(UserUpdateRequest request, Long userId) {
        User userToUpdate = finderByIdService.findById(userId);

        userToUpdate.setName(request.name());
        userToUpdate.setEmail(request.email());
        userToUpdate.setPhoneNumber(request.phoneNumber());
        return userRepository.save(userToUpdate);
    }
}
