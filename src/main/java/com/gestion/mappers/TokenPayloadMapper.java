package com.gestion.mappers;

import com.gestion.model.User;
import com.gestion.utils.TokenPayload;

public class TokenPayloadMapper {
    public static TokenPayload toTokenPayload(User user) {
        Long commerceId = user.getCommerce() != null ? user.getCommerce().getId() : null;
        return new TokenPayload(
                user.getEmail(),
                user.getName(),
                user.getId(),
                commerceId,
                user.getRole(),
                user.getActive()
        );
    }
}
