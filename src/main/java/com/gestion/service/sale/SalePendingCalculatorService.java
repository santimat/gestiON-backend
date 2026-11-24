package com.gestion.service.sale;

import com.gestion.dto.project.sale.SalePaymentSum;
import com.gestion.enums.SaleStatus;
import com.gestion.model.Sale;
import com.gestion.repository.JpaTransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SalePendingCalculatorService {

    private final JpaTransactionRepository jpaTransactionRepository;

    public BigDecimal calculateTotalPending(Sale sale) {
        if (sale == null || sale.getStatus() == SaleStatus.PAYED || sale.getStatus() == SaleStatus.GIFTED) {
            return null;
        }
        BigDecimal totalPaid = jpaTransactionRepository.sumPaymentsBySaleId(sale.getId());
        return sale.getTotal().subtract(totalPaid != null ? totalPaid : BigDecimal.ZERO);
    }


    // mapea id con total pendiente para listado de ventas, si la venta esta pagada o regalada el total pendiente es null
    public Map<Long, BigDecimal> calculateTotalPendingMap(Collection<Sale> sales) {
        if (sales == null || sales.isEmpty()) {
            return Map.of();
        }

        List<Long> pendingSaleIds = sales.stream()
                .filter(sale -> sale.getStatus() == SaleStatus.PENDING || sale.getStatus() == SaleStatus.PARTIAL_PAYED)
                .map(Sale::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, BigDecimal> paidBySaleId = pendingSaleIds.isEmpty()
                ? Map.of()
                : jpaTransactionRepository.sumPaymentsBySaleIds(pendingSaleIds).stream()
                        .collect(Collectors.toMap(
                                SalePaymentSum::saleId,
                                SalePaymentSum::totalPaid
                        ));

        Map<Long, BigDecimal> resultMap = new HashMap<>();
        for (Sale sale : sales) {
            if (sale.getId() == null) {
                continue;
            }
            if (sale.getStatus() == SaleStatus.PAYED || sale.getStatus() == SaleStatus.GIFTED) {
                resultMap.put(sale.getId(), null);
            } else {
                BigDecimal paid = paidBySaleId.getOrDefault(sale.getId(), BigDecimal.ZERO);
                resultMap.put(sale.getId(), sale.getTotal().subtract(paid));
            }
        }
        return resultMap;
    }
}
