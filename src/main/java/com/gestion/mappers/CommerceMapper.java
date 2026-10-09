package com.gestion.mappers;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.request.commerce.CommerceWithOwnerRequest;
import com.gestion.dto.request.commerce.CommerceWithOwnerUpdateRequest;
import com.gestion.dto.response.commerce.CurrentCommerceResponse;
import com.gestion.model.Commerce;
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
        commerce.setProfitMultiplier(request.profitMultiplier() != null ? request.profitMultiplier() : 1.0);
        return commerce;
    }

    public static CommerceRequest toRequestFromCWOR(CommerceWithOwnerRequest request, Double profitMultiplier) {
        return new CommerceRequest(
                request.businessName(),
                request.address(),
                request.cuit(),
                request.businessLogo(),
                profitMultiplier
        );
    }

    public static CommerceRequest toRequestFromCWOUR(CommerceWithOwnerUpdateRequest request) {
        return new CommerceRequest(
                request.businessName(),
                request.address(),
                request.cuit(),
                request.businessLogo(),
                request.profitMultiplier()
        );
    }

    public static CurrentCommerceResponse toCurrentResponse(Commerce commerce, @Nullable String businessLogoUrl) {
        return new CurrentCommerceResponse(
                commerce.getId(),
                commerce.getBusinessName(),
                businessLogoUrl,
                commerce.isActive(),
                commerce.getProfitMultiplier()
        );
    }
}
