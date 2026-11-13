package com.gestion.service.client;

import com.gestion.dto.project.ClientAccountSummary;
import com.gestion.dto.response.sale.PendingSalesWithDetailsResponse;
import com.gestion.dto.response.sale.SaleResponse;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleDetailRepository;
import com.gestion.repository.JpaSaleRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FindPendingSaleWithDetailsByClientIdService {

    private final JpaSaleRepository jpaSaleRepository;
    private final JpaSaleDetailRepository jpaSaleDetailRepository;
    private final ClientAccountSummaryService clientAccountSummaryService;

    @Transactional(readOnly = true)
    public PendingSalesWithDetailsResponse findPendingSalesWithDetailsByClientId(Long clientId,
                                                                               Long commerceId,
                                                                               Pageable pageable) {
        Page<Sale> pendingSales = jpaSaleRepository
                .findPendingByClientIdAndCommerceId(commerceId, clientId, pageable);

        // los detalles de toda la pagina salen en una sola query y se agrupan en memoria
        List<Long> saleIds = pendingSales.getContent().stream().map(Sale::getId).toList();
        Map<Long, List<SaleDetail>> detailsBySaleId = saleIds.isEmpty()
                ? Map.of()
                : jpaSaleDetailRepository.findAllBySaleIdIn(saleIds).stream()
                        .collect(Collectors.groupingBy(detail -> detail.getSale().getId()));

        Page<SaleResponse> saleResponses = pendingSales.map(sale ->
                SaleMapper.toResponse(sale, detailsBySaleId.getOrDefault(sale.getId(), List.of())));

        // el conteo y el total son de TODAS las ventas pendientes del cliente, no de la pagina
        ClientAccountSummary summary = clientAccountSummaryService
                .findByClientIds(List.of(clientId), commerceId)
                .get(clientId);

        return new PendingSalesWithDetailsResponse(
                summary.pendingSalesCount(),
                summary.totalPending(),
                saleResponses
        );
    }
}
