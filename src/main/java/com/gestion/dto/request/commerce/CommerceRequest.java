package com.gestion.dto.request.commerce;


import org.springframework.web.multipart.MultipartFile;

public record CommerceRequest(
        String businessName,
        String address,
        String phoneNumber,
        String cuit,
        MultipartFile logo
) {
}
