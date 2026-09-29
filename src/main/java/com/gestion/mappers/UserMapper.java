package com.gestion.mappers;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.commerce.CommerceWithOwnerRequest;
import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.AuthenticatedUserResponse;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.enums.Role;
import com.gestion.model.User;
import io.jsonwebtoken.Claims;

public class UserMapper {
    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getActive(),
                user.getPhoneNumber()
        );
    }

    public static UserRequest toRequest(CommerceWithOwnerRequest request) {
        return new UserRequest(
                request.username(),
                request.email(),
                request.password(),
                request.phoneNumber()
        );
    }

    public static AuthenticatedUserResponse toTokenResponseFromUserPrincipal(UserPrincipal authenticatedUser) {
        return new AuthenticatedUserResponse(
                authenticatedUser.getId(),
                authenticatedUser.getName(),
                authenticatedUser.getEmail(),
                authenticatedUser.getRole(),
                authenticatedUser.getActive()
        );
    }

    public static AuthenticatedUserResponse toAuthenticatedResponse(User user) {
        return new AuthenticatedUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getActive()
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
        user.setPhoneNumber(request.phoneNumber());
        return user;
    }

    public static AuthenticatedUserResponse toTokenResponseFromClaims(Claims userClaims) {
        Role userRole = Role.valueOf(userClaims.get("role", String.class));
        return new AuthenticatedUserResponse(
                userClaims.get("userId", Long.class),
                userClaims.get("name", String.class),
                userClaims.getSubject(),
                userRole,
                userClaims.get("active", Boolean.class)
        );
    }

}
