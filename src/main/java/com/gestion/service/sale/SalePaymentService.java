package com.gestion.service.sale;

import com.gestion.dto.response.transaction.TransactionResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.enums.SaleStatus;
import com.gestion.enums.TransactionType;
import com.gestion.exception.InvalidPaymentAmountException;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.exception.SaleAlreadyPayedException;
import com.gestion.mappers.TransactionMapper;
import com.gestion.model.Sale;
import com.gestion.model.Transaction;
import com.gestion.repository.JpaSaleRepository;
import com.gestion.repository.JpaTransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SalePaymentService {

    private final JpaSaleRepository jpaSaleRepository;
    private final JpaTransactionRepository jpaTransactionRepository;
    private final SalePendingCalculatorService salePendingCalculatorService;

    @Transactional
    public TransactionResponse registerPayment(Long saleId, Long commerceId, BigDecimal amount) {
        // 1. Buscar la venta con scope de comercio y bloquear su fila hasta el commit: un segundo
        // pago concurrente espera aca y despues calcula el pendiente con el primer pago ya registrado.
        Sale sale = jpaSaleRepository.findForUpdateByIdAndCommerceId(saleId, commerceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.SALE_NOT_FOUND,
                        "Sale with id " + saleId + " not found in this commerce"));

        // 2. Verificar que no esté ya totalmente pagada
        if (sale.getStatus() == SaleStatus.PAYED || sale.getStatus() == SaleStatus.GIFTED) {
            throw new SaleAlreadyPayedException();
        }

        // 3. Calcular pendiente con servicio centralizado
        BigDecimal pending = salePendingCalculatorService.calculateTotalPending(sale);

        // 4. Validar monto
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(pending) > 0) {
            throw new InvalidPaymentAmountException();
        }

        // 5. Crear transacción tipo PAGO
        Transaction payment = new Transaction();
        payment.setAmount(amount);
        payment.setType(TransactionType.PAYMENT);
        payment.setSale(sale);

        Transaction savedPayment = jpaTransactionRepository.save(payment);

        // 6. Actualizar status de la venta
        BigDecimal newPending = pending.subtract(amount);
        if (newPending.compareTo(BigDecimal.ZERO) == 0) {
            sale.setStatus(SaleStatus.PAYED);
        } else {
            sale.setStatus(SaleStatus.PARTIAL_PAYED);
        }
        jpaSaleRepository.save(sale);

        return TransactionMapper.toResponse(savedPayment);
    }
}