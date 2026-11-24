package com.gestion.mappers;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.user.AuthenticatedUserResponse;
import com.gestion.enums.Role;
import io.jsonwebtoken.Claims;

public class AuthenticatedUserMapper {
    public static AuthenticatedUserResponse toTokenResponseFromUserPrincipal(UserPrincipal authenticatedUser) {
        return new AuthenticatedUserResponse(
                authenticatedUser.getId(),
                authenticatedUser.getCommerceId(),
                authenticatedUser.getName(),
                authenticatedUser.getEmail(),
                authenticatedUser.getRole(),
                authenticatedUser.isActive(),
                authenticatedUser.getSellPointId()
        );
    }

    public static AuthenticatedUserResponse toTokenResponseFromClaims(Claims userClaims) {
        Role userRole = Role.valueOf(userClaims.get("role", String.class));
        return new AuthenticatedUserResponse(
                userClaims.get("userId", Long.class),
                userClaims.get("commerceId", Long.class),
                userClaims.get("name", String.class),
                userClaims.getSubject(),
                userRole,
                userClaims.get("active", Boolean.class),
                userClaims.get("sellPointId", Long.class)
        );
    }
}
