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
        commerce.setName(request.name());
        commerce.setAddress(request.address());
        commerce.setCuit(request.cuit());
        commerce.setProfitMultiplier(
                request.profitMultiplier() != null ? request.profitMultiplier() : Commerce.DEFAULT_PROFIT_MULTIPLIER);
        commerce.setDefaultMinStock(
                request.defaultMinStock() != null ? request.defaultMinStock() : Commerce.DEFAULT_MIN_STOCK);
        return commerce;
    }

    public static CommerceRequest toRequestFromCWOR(CommerceWithOwnerRequest request,
                                                      Double profitMultiplier,
                                                      Integer defaultMinStock) {
        return new CommerceRequest(
                request.name(),
                request.address(),
                request.cuit(),
                request.businessLogo(),
                profitMultiplier,
                defaultMinStock
        );
    }

    public static CommerceRequest toRequestFromCWOUR(CommerceWithOwnerUpdateRequest request) {
        return new CommerceRequest(
                request.name(),
                request.address(),
                request.cuit(),
                request.businessLogo(),
                request.profitMultiplier(),
                request.defaultMinStock()
        );
    }

    public static CurrentCommerceResponse toCurrentResponse(Commerce commerce, @Nullable String businessLogoUrl) {
        return new CurrentCommerceResponse(
                commerce.getId(),
                commerce.getName(),
                businessLogoUrl,
                commerce.isActive(),
                commerce.getProfitMultiplier(),
                commerce.getDefaultMinStock()
        );
    }
}
