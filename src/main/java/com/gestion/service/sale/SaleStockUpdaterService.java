package com.gestion.service.sale;

import com.gestion.model.Product;
import com.gestion.model.SaleDetail;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class SaleStockUpdaterService {

    public void applyStockDecrement(List<SaleDetail> details) {
        for (SaleDetail detail : details) {
            Product product = detail.getProduct();
            product.setCurrentStock(product.getCurrentStock() - detail.getQuantity());
        }
    }
}
