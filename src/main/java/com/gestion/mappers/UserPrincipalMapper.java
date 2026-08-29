package com.gestion.mappers;

import com.gestion.config.UserPrincipal;
import com.gestion.enums.Role;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

@Component
public class UserPrincipalMapper {
    public UserPrincipal toEntity(Claims tokenClaims) {
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setEmail(tokenClaims.getSubject());
        userPrincipal.setUserId(tokenClaims.get("userId", Long.class));
        userPrincipal.setName(tokenClaims.get("name", String.class));
        userPrincipal.setRole(tokenClaims.get("role", Role.class));
        return userPrincipal;
    }
}
