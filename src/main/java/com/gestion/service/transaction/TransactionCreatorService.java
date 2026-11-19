package com.gestion.service.transaction;

import com.gestion.dto.request.transaction.TransactionRequest;
import com.gestion.dto.response.transaction.TransactionResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.mappers.TransactionMapper;
import com.gestion.model.Sale;
import com.gestion.model.Transaction;
import com.gestion.repository.JpaSaleRepository;
import com.gestion.repository.JpaTransactionRepository;
import com.gestion.service.sale.SaleFinderByIdAndCommerceIdService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class TransactionCreatorService {

    private final JpaTransactionRepository jpaTransactionRepository;
    private final SaleFinderByIdAndCommerceIdService saleFinderByIdAndCommerceIdService;

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request, Long commerceId) {
        // Validación de negocio: al menos revenue o outflow debe ser > 0
        BigDecimal revenue = request.revenue() != null ? request.revenue() : BigDecimal.ZERO;
        BigDecimal outflow = request.outflow() != null ? request.outflow() : BigDecimal.ZERO;

        if (revenue.compareTo(BigDecimal.ZERO) == 0 && outflow.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("At least one of revenue or outflow must be greater than zero");
        }

        // Buscar la sale y verificar que pertenezca al comercio
        Sale sale = saleFinderByIdAndCommerceIdService.findSaleByIdAndCommerceId(request.saleId(), commerceId);

        // Crear la transacción usando el mapper
        Transaction transaction = TransactionMapper.toEntity(request, sale);

        Transaction savedTransaction = jpaTransactionRepository.save(transaction);

        return TransactionMapper.toResponse(savedTransaction);
    }
}