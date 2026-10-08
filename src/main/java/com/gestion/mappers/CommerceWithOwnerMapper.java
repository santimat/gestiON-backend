package com.gestion.mappers;

import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import jakarta.annotation.Nullable;

public class CommerceWithOwnerMapper {
    public static CommerceWithOwnerResponse toCommerceWithOwnerResponse(Commerce commerce, User user,
                                                                        @Nullable String businessLogoUrl) {
        return new CommerceWithOwnerResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                commerce.getId(),
                commerce.getBusinessName(),
                commerce.getCuit(),
                commerce.getAddress(),
                businessLogoUrl,
                commerce.isActive(),
                commerce.getProfitMultiplier(),
                commerce.getUpdatedAt()
        );
    }
}
