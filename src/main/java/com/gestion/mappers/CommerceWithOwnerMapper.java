package com.gestion.mappers;

import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import jakarta.annotation.Nullable;

public class CommerceWithOwnerMapper {
    // owner puede ser null: un comercio sin owner se lista igual, con los datos del owner en null
    public static CommerceWithOwnerResponse toCommerceWithOwnerResponse(Commerce commerce, @Nullable User owner,
                                                                        @Nullable String businessLogoUrl) {
        return new CommerceWithOwnerResponse(
                owner != null ? owner.getId() : null,
                owner != null ? owner.getName() : null,
                owner != null ? owner.getEmail() : null,
                owner != null ? owner.getPhoneNumber() : null,
                commerce.getId(),
                commerce.getName(),
                commerce.getCuit(),
                commerce.getAddress(),
                businessLogoUrl,
                commerce.isActive(),
                commerce.getProfitMultiplier(),
                commerce.getUpdatedAt()
        );
    }
}
