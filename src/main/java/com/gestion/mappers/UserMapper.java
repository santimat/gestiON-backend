package com.gestion.mappers;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.model.User;
import org.springframework.stereotype.Component;

// Esta anotación se agrega para que springboot escanee este componente y pueda inyectarlo a la hora de usarlo.
// Esto nos evita tener que instanciar el mapper en cada clase que lo usemos.
@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    public User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setRole(request.role());
        user.setStatus(request.status());
        return user;
    }
}
