package com.gestion.mappers;

import com.gestion.dto.response.commerce.CommerceProfitMultiplierResponse;
import com.gestion.model.Commerce;

public class CommerceProfitMultiplierMapper {

    public static CommerceProfitMultiplierResponse toResponse(Commerce commerce) {
        if (commerce == null) {
            return null;
        }
        return new CommerceProfitMultiplierResponse(
                commerce.getId(),
                commerce.getProfitMultiplier()
        );
    }
}
