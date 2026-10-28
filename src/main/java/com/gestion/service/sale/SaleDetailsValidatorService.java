package com.gestion.service.sale;

import com.gestion.model.Product;
import com.gestion.model.SaleDetail;
import com.gestion.service.product.ProductFinderByIdAndCommerceIdService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SaleDetailsValidatorService {
    private final ProductFinderByIdAndCommerceIdService productFinderByIdAndCommerceIdService;
    private final SaleProductValidatorService saleProductValidatorService;
    private final SaleStockValidatorService saleStockValidatorService;


    public SaleDetailsValidationResult validate(Long commerceId, Map<Long, Integer> quantitiesByProductId) {
        List<SaleDetail> details = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : quantitiesByProductId.entrySet()) {
            Product product = productFinderByIdAndCommerceIdService
                    .findProductByIdAndCommerceId(entry.getKey(), commerceId);

            saleProductValidatorService.validateProductIsAvailable(product);
            saleStockValidatorService.validateStock(product, entry.getValue());

            SaleDetail detail = new SaleDetail();
            detail.setProduct(product);
            detail.setPrice(product.getSalePrice());
            detail.setQuantity(entry.getValue());

            details.add(detail);
            subtotal = subtotal.add(product.getSalePrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }

        return new SaleDetailsValidationResult(details, subtotal);
    }


}
