package com.gestion.controller.commerce;

import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.service.commerce.CommerceFindAllWithOwnerService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/commerces")
@AllArgsConstructor

public class CommercesWithOwnerGetController {

    private final CommerceFindAllWithOwnerService commerceFindAllWithOwnerService;

    @GetMapping
    @PreAuthorize("hasRole('SUDO')")
    public ResponseEntity<Page<CommerceWithOwnerResponse>> getAllCommercesWithOwner(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "DESC") String sortOrder,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Sort.Direction sortDirection = Sort.Direction.fromString(sortOrder);
        Sort sortConfig = Sort.by(sortDirection, sortBy);
        Pageable pageable = PageRequest.of(page, size, sortConfig);
        return ResponseEntity.ok(commerceFindAllWithOwnerService.findAllWithOwner(pageable));
    }
}
