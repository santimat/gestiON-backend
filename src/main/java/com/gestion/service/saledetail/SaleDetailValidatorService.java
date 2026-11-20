package com.gestion.service.saledetail;

import com.gestion.model.Product;
import com.gestion.model.SaleDetail;
import com.gestion.service.product.ProductFinderByIdAndCommerceIdService;
import com.gestion.service.sale.SaleProductValidatorService;
import com.gestion.service.sale.SaleStockValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SaleDetailValidatorService {
    private final ProductFinderByIdAndCommerceIdService productFinder;
    private final SaleProductValidatorService productValidator;
    private final SaleStockValidatorService stockValidator;
    private final SaleDetailPriceCalculator priceCalculator;

    public SaleDetailValidationResult validate(Long commerceId, Map<Long, Integer> quantitiesByProductId) {
        List<SaleDetail> details = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : quantitiesByProductId.entrySet()) {
            Product product = productFinder.findProductByIdAndCommerceId(entry.getKey(), commerceId);

            productValidator.validateProductIsAvailable(product);
            stockValidator.validateStock(product, entry.getValue());

            SaleDetailPriceCalculator.PriceCalculationResult priceResult = priceCalculator.calculatePrice(product);

            SaleDetail detail = new SaleDetail();
            detail.setProduct(product);
            detail.setPrice(priceResult.unitPrice());
            detail.setQuantity(entry.getValue());

            priceCalculator.applyOfferSnapshot(detail, priceResult.activeOffer());

            details.add(detail);
            subtotal = subtotal.add(priceResult.unitPrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }

        return new SaleDetailValidationResult(details, subtotal);
    }
}