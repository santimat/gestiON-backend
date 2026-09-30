package com.gestion.controller.commerce;

import com.gestion.dto.response.commerce.CommerceUpdateActiveResponse;
import com.gestion.service.commerce.CommerceUpdaterActiveService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commerces")
@AllArgsConstructor
public class CommerceToggleActivePatchController {
    private final CommerceUpdaterActiveService commerceUpdaterActiveService;

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SUDO')")
    public ResponseEntity<CommerceUpdateActiveResponse> updateActive(@PathVariable Long id) {
        return ResponseEntity.ok(commerceUpdaterActiveService.toggleActive(id));
    }

}
