package com.gestion.mappers;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.project.user.UserSessionSnapshot;

public class UserPrincipalMapper {
    // el principal sale del snapshot de la base, no de los claims del JWT: el token solo identifica
    // al usuario (userId) y prueba que se autentico
    public static UserPrincipal toEntity(UserSessionSnapshot snapshot) {
        UserPrincipal userPrincipal = new UserPrincipal();
        userPrincipal.setId(snapshot.userId());
        userPrincipal.setEmail(snapshot.email());
        userPrincipal.setName(snapshot.name());
        userPrincipal.setRole(snapshot.role());
        userPrincipal.setActive(snapshot.userActive());
        userPrincipal.setCommerceId(snapshot.commerceId());
        userPrincipal.setSellPointId(snapshot.sellPointId());
        return userPrincipal;
    }
}
