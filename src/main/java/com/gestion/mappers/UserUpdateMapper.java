package com.gestion.mappers;

import com.gestion.dto.request.commerce.CommerceWithOwnerRequest;
import com.gestion.dto.request.commerce.CommerceWithOwnerUpdateRequest;
import com.gestion.dto.request.user.UserUpdateRequest;

public class UserUpdateMapper {
    public static UserUpdateRequest toUpdateRequestFromCWOUR(CommerceWithOwnerUpdateRequest request) {
        return new UserUpdateRequest(
                request.username(),
                request.email(),
                request.phoneNumber()
        );
    }

    public static UserUpdateRequest toUpdateRequest(CommerceWithOwnerRequest request) {
        return new UserUpdateRequest(
                request.username(),
                request.email(),
                request.phoneNumber()
        );
    }
}
