package com.gestion.mappers;

import com.gestion.dto.response.commerce.CommerceDefaultMinStockResponse;
import com.gestion.model.Commerce;

public class CommerceDefaultMinStockMapper {

    public static CommerceDefaultMinStockResponse toResponse(Commerce commerce) {
        return new CommerceDefaultMinStockResponse(
                commerce.getId(),
                commerce.getDefaultMinStock()
        );
    }
}
