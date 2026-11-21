package com.gestion.service.sale;

import com.gestion.dto.response.sale.SaleSummaryResponse;
import com.gestion.enums.PaymentMethod;
import com.gestion.enums.SaleStatus;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Sale;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class SaleSearcherService {
    private final JpaSaleRepository jpaSaleRepository;
    private final SaleItemQuantitiesService saleItemQuantitiesService;
    private final SalePendingCalculatorService salePendingCalculatorService;

    @Transactional(readOnly = true)
    public Page<SaleSummaryResponse> searchSales(Long commerceId, Long clientId,
                                                 SaleStatus status, PaymentMethod paymentMethod, String searchText,
                                                 Pageable pageable) {
        String normalizedSearchText = (searchText != null && searchText.isBlank()) ? null : searchText;

        Page<Sale> sales = jpaSaleRepository.findAllWithFilters(
                commerceId, clientId, status, paymentMethod, normalizedSearchText, pageable);

        List<Long> saleIds = sales.getContent().stream().map(Sale::getId).filter(Objects::nonNull).toList();
        Map<Long, Long> itemCountsBySaleId = saleItemQuantitiesService.mapSaleItemQuantities(saleIds);
        Map<Long, BigDecimal> pendingBySaleId = salePendingCalculatorService.calculateTotalPendingMap(sales.getContent());

        return sales.map(sale -> SaleMapper.toSummaryResponse(
                sale,
                itemCountsBySaleId.getOrDefault(sale.getId(), 0L).intValue(),
                pendingBySaleId.get(sale.getId())
        ));
    }
}
