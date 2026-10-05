package com.gestion.controller.commerce;

import com.gestion.dto.response.commerce.CommerceStatsResponse;
import com.gestion.service.commerce.CommerceStatsGetterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commerces/stats")
@AllArgsConstructor
public class CommerceStatsGetController {
    private final CommerceStatsGetterService commerceStatsGetterService;

    @GetMapping
    @PreAuthorize("hasRole('SUDO')")
    public ResponseEntity<CommerceStatsResponse> getStats() {
        return ResponseEntity.ok(commerceStatsGetterService.getStats());
    }
}
