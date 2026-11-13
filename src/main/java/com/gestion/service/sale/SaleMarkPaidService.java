package com.gestion.service.sale;

import com.gestion.dto.response.sale.SaleUpdateStatusResponse;
import com.gestion.enums.SaleStatus;
import com.gestion.exception.SaleNotPendingException;
import com.gestion.model.Sale;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SaleMarkPaidService {
    private final SaleFinderByIdAndCommerceIdService saleFinderByIdAndCommerceIdService;

    @Transactional
    public SaleUpdateStatusResponse markPaid(Long id, Long commerceId) {
        Sale sale = saleFinderByIdAndCommerceIdService.findSaleByIdAndCommerceId(id, commerceId);
        
        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new SaleNotPendingException(
                    "Sale with id " + id + " is not pending");
        }

        sale.setStatus(SaleStatus.PAYED);
        return new SaleUpdateStatusResponse(sale.getId(), sale.getStatus());
    }
}
