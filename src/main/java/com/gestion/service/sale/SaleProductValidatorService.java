package com.gestion.service.sale;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InactiveResourceException;
import com.gestion.model.Product;
import org.springframework.stereotype.Service;

@Service
public class SaleProductValidatorService {

    public void validateProductIsAvailable(Product product) {
        if (!product.isActive()) {
            throw new InactiveResourceException(ErrorCode.PRODUCT_INACTIVE,
                    "Product '" + product.getName() + "' is not available for sale");
        }
    }
}
