package com.gestion.service.sale;

import com.gestion.config.UserPrincipal;
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

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SaleSearcherService {
    private final JpaSaleRepository jpaSaleRepository;
    private final SaleItemQuantitiesMapperService saleItemQuantitiesMapperService;

    @Transactional(readOnly = true)
    public Page<SaleSummaryResponse> searchSales(UserPrincipal authenticatedUser, Long clientId,
                                                 SaleStatus status, PaymentMethod paymentMethod, String searchText,
                                                 Pageable pageable) {
        String normalizedSearchText = (searchText != null && searchText.isBlank()) ? null : searchText;

        Page<Sale> sales = jpaSaleRepository.findAllWithFilters(
                authenticatedUser.getCommerceId(), clientId, status, paymentMethod, normalizedSearchText, pageable);

        List<Long> saleIds = sales.getContent().stream().map(Sale::getId).toList();
        Map<Long, Long> itemCountsBySaleId = saleItemQuantitiesMapperService.mapSaleItemQuantities(saleIds);

        return sales.map(sale -> SaleMapper.toSummaryResponse(sale,
                itemCountsBySaleId.getOrDefault(sale.getId(), 0L).intValue()));
    }
}
