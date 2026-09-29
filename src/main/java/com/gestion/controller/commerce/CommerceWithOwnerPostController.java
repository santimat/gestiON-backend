package com.gestion.controller.commerce;

import com.gestion.dto.request.commerce.CommerceWithOwnerRequest;
import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.service.commerce.CommerceWithOwnerCreatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commerces-users")
@AllArgsConstructor
public class CommerceWithOwnerPostController {
    private final CommerceWithOwnerCreatorService commerceWithOwnerCreatorService;

    @PostMapping
    public ResponseEntity<CommerceWithOwnerResponse> createCommerceWithOwner(@ModelAttribute @Valid CommerceWithOwnerRequest commerceWithOwnerRequest) {
        CommerceWithOwnerResponse response = commerceWithOwnerCreatorService.createCommerceWithOwner(commerceWithOwnerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
