package com.gestion.service.user;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserUpdaterService {
    private JpaUserRepository userRepository;
    private UserFinderByEmailService userFinderByEmailService;

    public UserResponse updateUser(UserRequest userRequest) {
        userFinderByEmailService.findByEmail(userRequest.email());

    }
}
