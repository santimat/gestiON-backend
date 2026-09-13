package com.gestion.mappers;

import com.gestion.model.User;
import com.gestion.utils.TokenPayload;

public class TokenPayloadMapper {
    public static TokenPayload toTokenPayload(User user) {
        return new TokenPayload(
                user.getEmail(),
                user.getName(),
                user.getId(),
                user.getRole(),
                user.getStatus()
        );
    }
}
