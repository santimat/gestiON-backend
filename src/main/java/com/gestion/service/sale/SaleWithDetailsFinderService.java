package com.gestion.service.sale;

import com.gestion.dto.response.sale.SaleResponse;
import com.gestion.enums.SaleStatus;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleDetailRepository;
import com.gestion.repository.JpaTransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class SaleWithDetailsFinderService {
    private final JpaSaleDetailRepository saleDetailRepository;
    private final SaleFinderByIdAndCommerceIdService saleFinderByIdAndCommerceIdService;
    private final JpaTransactionRepository jpaTransactionRepository;

    public SaleResponse findBySaleIdAndCommerceId(Long saleId, Long commerceId) {
        Sale sale = saleFinderByIdAndCommerceIdService.findSaleByIdAndCommerceId(saleId, commerceId);
        List<SaleDetail> saleDetails = saleDetailRepository.findAllBySaleId(sale.getId());

        BigDecimal totalPending = calculateTotalPending(sale);
        return SaleMapper.toResponse(sale, saleDetails, totalPending);
    }

    private BigDecimal calculateTotalPending(Sale sale) {
        if (sale.getStatus() == SaleStatus.PAYED || sale.getStatus() == SaleStatus.GIFTED) {
            return null;
        }
        BigDecimal totalPaid = jpaTransactionRepository.sumPaymentsBySaleId(sale.getId());
        return sale.getTotal().subtract(totalPaid);
    }
}
