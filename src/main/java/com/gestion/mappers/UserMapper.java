package com.gestion.mappers;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.dto.response.user.UserTokenResponse;
import com.gestion.enums.Role;
import com.gestion.enums.UserStatus;
import com.gestion.model.User;
import io.jsonwebtoken.Claims;

public class UserMapper {
    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getPhoneNumber()
        );
    }

    public static UserTokenResponse toTokenResponse(User user) {
        return new UserTokenResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }

    public static User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        return user;
    }

    public static UserTokenResponse toResponseFromClaims(Claims userClaims) {
        Role userRole = Role.valueOf(userClaims.get("role", String.class));
        UserStatus userStatus = UserStatus.valueOf(userClaims.get("status", String.class));
        return new UserTokenResponse(
                userClaims.get("userId", Long.class),
                userClaims.get("name", String.class),
                userClaims.getSubject(),
                userRole,
                userStatus
        );
    }
}
