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
import java.util.Objects;

@Service
@AllArgsConstructor
public class FindPendingSaleWithDetailsByClientIdService {

    private final JpaSaleRepository jpaSaleRepository;
    private final JpaSaleDetailRepository jpaSaleDetailRepository;

    @Transactional(readOnly = true)
    public PendingSalesWithDetailsResponse findPendingSalesWithDetailsByClientId(Long clientId, Long commerceId) {
        Pageable pageable = Pageable.unpaged(Sort.by(Sort.Direction.ASC, "createdAt"));

        List<Sale> pendingSales = jpaSaleRepository
                .findAllWithFilters(commerceId, clientId, SaleStatus.PENDING, null, null, pageable)
                .getContent();

        // el conteo y el total salen de la misma lista: no hace falta una query extra de resumen
        long pendingSalesCount = pendingSales.size();
        BigDecimal totalPending = pendingSales.stream()
                .map(Sale::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SaleResponse> pendingSaleResponses = pendingSales.stream()
                .map(sale -> SaleMapper.toResponse(sale, jpaSaleDetailRepository.findAllBySaleId(sale.getId())))
                .toList();

        return new PendingSalesWithDetailsResponse(pendingSalesCount, totalPending, pendingSaleResponses);
    }
}