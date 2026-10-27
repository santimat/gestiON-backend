package com.gestion.service.sale;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InsufficientStockException;
import com.gestion.model.Product;
import org.springframework.stereotype.Service;


@Service
public class SaleStockValidatorService {

    public void validateStock(Product product, Integer quantity) {
        if (product.getCurrentStock() < quantity) {
            throw new InsufficientStockException(ErrorCode.INSUFFICIENT_STOCK,
                    "Not enough stock for product '" + product.getName()
                            + "' (requested: " + quantity + ", available: " + product.getCurrentStock() + ")");
        }
    }
}
