package com.gestion.controller.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sale.SaleResponse;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Sale;
import com.gestion.repository.JpaSaleDetailRepository;
import com.gestion.service.sale.SaleFinderByIdAndCommerceIdService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales")
@AllArgsConstructor
public class SaleGetController {
    private final SaleFinderByIdAndCommerceIdService saleFinderByIdAndCommerceIdService;
    private final JpaSaleDetailRepository jpaSaleDetailRepository;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CASHIER') or hasRole('OWNER')")
    public ResponseEntity<SaleResponse> getSale(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {
        Sale sale = saleFinderByIdAndCommerceIdService.findBy(id, authenticatedUser.getCommerceId());
        return ResponseEntity.ok(SaleMapper.toResponse(
                sale,
                jpaSaleDetailRepository.findAllBySaleId(sale.getId())));
    }
}
