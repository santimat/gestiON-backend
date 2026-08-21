package com.gestion.dto.response.user;

import com.gestion.enums.Role;
import com.gestion.enums.UserStatus;
import com.gestion.model.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        UserStatus status) {

    public static UserResponse toResponse(User user) {
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
}
// a chequar si esta bien. fui ayudada por Gemini