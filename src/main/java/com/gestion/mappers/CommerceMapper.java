package com.gestion.mappers;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.response.commerce.CommerceResponse;
import com.gestion.model.Commerce;
import org.springframework.stereotype.Component;

@Component
public class CommerceMapper {
    public CommerceResponse toResponse(Commerce commerce) {
        return new CommerceResponse(
                commerce.getId(),
                commerce.getBusinessName(),
                commerce.getAddress(),
                commerce.getPhoneNumber(),
                commerce.getCuit(),
                commerce.getLogoUrl()
        );
    }

    public Commerce toEntity(CommerceRequest request) {
        Commerce commerce = new Commerce();
        commerce.setBusinessName(request.businessName());
        commerce.setAddress(request.address());
        commerce.setPhoneNumber(request.phoneNumber());
        commerce.setCuit(request.cuit());
        return commerce;
    }
}
