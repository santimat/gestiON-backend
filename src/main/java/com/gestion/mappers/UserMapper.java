package com.gestion.mappers;

import com.gestion.dto.request.commerce.CommerceWithOwnerRequest;
import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.model.User;

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

    public static UserRequest toRequestFromCWOR(CommerceWithOwnerRequest request) {
        return new UserRequest(
                request.username(),
                request.email(),
                request.password(),
                request.phoneNumber()
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
}
