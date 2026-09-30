package com.gestion.mappers;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.request.commerce.CommerceWithOwnerRequest;
import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import jakarta.annotation.Nullable;

public class CommerceMapper {
    public static Commerce toEntity(CommerceRequest request) {
        if (request == null) {
            return null;
        }
        Commerce commerce = new Commerce();
        commerce.setBusinessName(request.businessName());
        commerce.setAddress(request.address());
        commerce.setCuit(request.cuit());
        return commerce;
    }

    public static CommerceRequest toRequest(CommerceWithOwnerRequest request) {
        return new CommerceRequest(
                request.businessName(),
                request.address(),
                request.cuit(),
                request.businessLogo()
        );
    }

    public static CommerceWithOwnerResponse toCommerceWithOwnerResponse(Commerce commerce, User user,
                                                                        @Nullable String businessLogoUrl) {
        return new CommerceWithOwnerResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                commerce.getId(),
                commerce.getBusinessName(),
                commerce.getAddress(),
                businessLogoUrl,
                commerce.getCuit(),
                commerce.isActive(),
                commerce.getCreatedAt()
        );
    }
}
