package com.gestion.mappers;

import com.gestion.dto.request.transaction.TransactionRequest;
import com.gestion.dto.response.transaction.TransactionResponse;
import com.gestion.model.Sale;
import com.gestion.model.Transaction;

import java.math.BigDecimal;

public class TransactionMapper {

    public static Transaction toEntity(TransactionRequest request, Sale sale) {
        Transaction transaction = new Transaction();
        transaction.setAmount(request.amount() != null ? request.amount() : BigDecimal.ZERO);
        transaction.setType(request.type());
        transaction.setSale(sale);
        return transaction;
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getSale() != null ? transaction.getSale().getId() : null,
                transaction.getCreatedAt()
        );
    }
}