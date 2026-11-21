package com.gestion.service.transaction;

import com.gestion.dto.response.transaction.TransactionResponse;
import com.gestion.enums.TransactionType;
import com.gestion.mappers.TransactionMapper;
import com.gestion.model.Sale;
import com.gestion.model.Transaction;
import com.gestion.repository.JpaTransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TransactionCreatorService {

    private final JpaTransactionRepository jpaTransactionRepository;

    @Transactional
    public TransactionResponse createSaleTransaction(Sale sale) {
        // Crear transacción tipo VENTA con el total de la venta
        Transaction transaction = new Transaction();
        transaction.setAmount(sale.getTotal());
        transaction.setType(TransactionType.SALE);
        transaction.setSale(sale);

        Transaction savedTransaction = jpaTransactionRepository.save(transaction);

        return TransactionMapper.toResponse(savedTransaction);
    }
}