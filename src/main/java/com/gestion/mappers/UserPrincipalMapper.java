package com.gestion.mappers;

import com.gestion.config.UserPrincipal;
import com.gestion.enums.Role;
import io.jsonwebtoken.Claims;

public class UserPrincipalMapper {
    public static UserPrincipal toEntity(Claims tokenClaims) {
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setEmail(tokenClaims.getSubject());
        userPrincipal.setId(tokenClaims.get("userId", Long.class));
        userPrincipal.setCommerceId(tokenClaims.get("commerceId", Long.class));
        userPrincipal.setName(tokenClaims.get("name", String.class));
        userPrincipal.setRole(Role.valueOf(tokenClaims.get("role", String.class)));
        userPrincipal.setActive(tokenClaims.get("active", Boolean.class));
        return userPrincipal;
    }
}
