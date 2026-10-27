package com.gestion.service.sale;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InactiveResourceException;
import com.gestion.model.Product;
import org.springframework.stereotype.Service;

/**
 * Valida que el producto esté disponible para venta (activo). Los productos
 * desactivados no pueden venderse aunque tengan stock.
 */
@Service
public class SaleProductValidatorService {

    public void validateProductIsAvailable(Product product) {
        if (!product.isActive()) {
            throw new InactiveResourceException(ErrorCode.PRODUCT_INACTIVE,
                    "Product '" + product.getName() + "' is not available for sale");
        }
    }
}
