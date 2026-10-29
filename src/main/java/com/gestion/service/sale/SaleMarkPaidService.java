package com.gestion.service.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.sale.SaleUpdateStatusResponse;
import com.gestion.enums.SaleStatus;
import com.gestion.exception.SaleNotPendingException;
import com.gestion.model.Sale;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SaleMarkPaidService {
    private final SaleFinderByIdAndCommerceIdService saleFinderByIdAndCommerceIdService;

    @Transactional
    public SaleUpdateStatusResponse markPaid(Long id, UserPrincipal authenticatedUser) {
        Sale sale = saleFinderByIdAndCommerceIdService.findBy(id, authenticatedUser.getCommerceId());
        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new SaleNotPendingException(
                    "Sale with id " + id + " is not pending");
        }

        sale.setStatus(SaleStatus.PAY);
        return new SaleUpdateStatusResponse(sale.getId(), sale.getStatus());
    }
}
