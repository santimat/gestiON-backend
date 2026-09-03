package com.gestion.controller.commerce;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.response.commerce.CommerceResponse;
import com.gestion.service.commerce.CommerceCreatorService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commerces")
@AllArgsConstructor
public class CommercePostController {
    private final CommerceCreatorService commerceCreatorService;

    @PostMapping()
    public ResponseEntity<CommerceResponse> createCommerce(@RequestBody CommerceRequest request) {
        CommerceResponse newCommerce = commerceCreatorService.createCommerce(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCommerce);
    }
}
