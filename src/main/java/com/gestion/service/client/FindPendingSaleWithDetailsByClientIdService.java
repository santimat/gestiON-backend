package com.gestion.service.client;

import com.gestion.dto.response.sale.PendingSalesWithDetailsResponse;
import com.gestion.dto.response.sale.SaleResponse;
import com.gestion.enums.SaleStatus;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Sale;
import com.gestion.repository.JpaSaleDetailRepository;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class FindPendingSaleWithDetailsByClientIdService {

    private final JpaSaleRepository jpaSaleRepository;
    private final JpaSaleDetailRepository jpaSaleDetailRepository;
    private final ClientSaleStatsService clientSaleStatsService;

    @Transactional(readOnly = true)
    public PendingSalesWithDetailsResponse findPendingSalesWithDetailsByClientId(Long clientId, Long commerceId) {
        JpaSaleRepository.ClientSalesSummary stats = clientSaleStatsService.getClientSaleStats(clientId, commerceId);

        Long pendingSalesCount = stats != null && stats.getPendingSalesCount() != null
                ? stats.getPendingSalesCount().longValue()
                : 0L;
        BigDecimal totalPending = stats != null && stats.getTotalPending() != null
                ? stats.getTotalPending()
                : BigDecimal.ZERO;

        Pageable pageable = Pageable.unpaged(Sort.by(Sort.Direction.ASC, "createdAt"));

        List<Sale> pendingSales = jpaSaleRepository
                .findAllWithFilters(commerceId, clientId, SaleStatus.PENDING, null, null, pageable)
                .getContent();

        List<SaleResponse> pendingSaleResponses = pendingSales.stream()
                .map(sale -> SaleMapper.toResponse(sale, jpaSaleDetailRepository.findAllBySaleId(sale.getId())))
                .toList();

        return new PendingSalesWithDetailsResponse(pendingSalesCount, totalPending, pendingSaleResponses);
    }
}