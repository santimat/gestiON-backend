package com.gestion.mappers;

import com.gestion.config.UserPrincipal;
import com.gestion.enums.Role;
import com.gestion.enums.UserStatus;
import io.jsonwebtoken.Claims;

public class UserPrincipalMapper {
    public static UserPrincipal toEntity(Claims tokenClaims) {
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setEmail(tokenClaims.getSubject());
        userPrincipal.setId(tokenClaims.get("userId", Long.class));
        userPrincipal.setName(tokenClaims.get("name", String.class));
        userPrincipal.setRole(Role.valueOf(tokenClaims.get("role", String.class)));
        userPrincipal.setStatus(UserStatus.valueOf(tokenClaims.get("status", String.class)));
        return userPrincipal;
    }
}
