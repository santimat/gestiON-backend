package com.gestion.service.sale;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.ConcurrentStockUpdateException;
import com.gestion.model.Product;
import com.gestion.model.SaleDetail;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SaleStockUpdaterService {

    public void applyStockDecrement(List<SaleDetail> details) {
        for (SaleDetail detail : details) {
            Product product = detail.getProduct();
            try {
                product.setCurrentStock(product.getCurrentStock() - detail.getQuantity());
            } catch (OptimisticLockException e) {
                throw new ConcurrentStockUpdateException(ErrorCode.CONCURRENT_STOCK_UPDATE,
                        "Stock for product '" + product.getName() + "' was modified by another transaction, please retry");
            }
        }
    }
}
