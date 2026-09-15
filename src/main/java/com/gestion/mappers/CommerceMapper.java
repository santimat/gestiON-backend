package com.gestion.mappers;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.response.commerce.CommerceResponse;
import com.gestion.model.Commerce;

public class CommerceMapper {
    public static CommerceResponse toResponse(Commerce commerce) {
        return new CommerceResponse(
                commerce.getId(),
                commerce.getBusinessName(),
                commerce.getAddress(),
                commerce.getCuit(),
                commerce.getLogoName()
        );
    }

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
}
