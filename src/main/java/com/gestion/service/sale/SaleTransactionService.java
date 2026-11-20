package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.enums.TransactionType;
import com.gestion.model.Sale;
import com.gestion.model.Transaction;
import com.gestion.repository.JpaTransactionRepository;
import com.gestion.service.transaction.TransactionCreatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SaleTransactionService {
    private final SaleRevenueOutflowCalculatorService calculator;
    private final TransactionCreatorService transactionCreator;
    private final JpaTransactionRepository jpaTransactionRepository;

    public void createIfNeeded(SaleRequest request, Sale savedSale, Long commerceId) {
        var result = calculator.calculate(savedSale);
        if (result.shouldCreateTransaction()) {
            // 1. Crear transacción VENTA (total de la venta)
            transactionCreator.createSaleTransaction(savedSale);

            // 2. Si es CREDIT_ACCOUNT y hay partiallyPaidAmount > 0, crear PAGO inicial
            if (savedSale.getPaymentMethod() == com.gestion.enums.PaymentMethod.CREDIT_ACCOUNT
                    && request.partiallyPaidAmount() != null
                    && request.partiallyPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal paid = request.partiallyPaidAmount();
                Transaction payment = new Transaction();
                payment.setAmount(paid);
                payment.setType(TransactionType.PAGO);
                payment.setSale(savedSale);
                jpaTransactionRepository.save(payment);

                // Actualizar status de la venta
                BigDecimal pending = savedSale.getTotal().subtract(paid);
                if (pending.compareTo(BigDecimal.ZERO) == 0) {
                    savedSale.setStatus(com.gestion.enums.SaleStatus.PAYED);
                } else {
                    savedSale.setStatus(com.gestion.enums.SaleStatus.PARTIAL_PAYED);
                }
            }
        }
    }
}