package com.gestion.mappers;

import com.gestion.dto.request.cashier.CashierRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.model.User;

public class CashierMapper {
    public static User toEntity(CashierRequest request) {
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

    public static UserResponse toResponse(User user) {
        return UserMapper.toResponse(user);
    }
}
