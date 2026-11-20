package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.request.transaction.TransactionRequest;
import com.gestion.model.Sale;
import com.gestion.service.transaction.TransactionCreatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SaleTransactionService {
    private final SaleRevenueOutflowCalculatorService calculator;
    private final TransactionCreatorService transactionCreator;

    public void createIfNeeded(SaleRequest request, Sale savedSale, Long commerceId) {
        SaleRevenueOutflowCalculatorService.Result result = calculator.calculate(request, savedSale);
        if (result.revenue().compareTo(BigDecimal.ZERO) > 0 || result.outflow().compareTo(BigDecimal.ZERO) > 0) {
            TransactionRequest txRequest = new TransactionRequest(result.revenue(), result.outflow(), savedSale.getId());
            transactionCreator.createTransaction(txRequest, commerceId);
        }
    }
}