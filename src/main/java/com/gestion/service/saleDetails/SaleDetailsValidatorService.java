package com.gestion.service.saleDetails;

import com.gestion.model.Offer;
import com.gestion.model.Product;
import com.gestion.model.SaleDetail;
import com.gestion.service.product.ProductActiveOfferFinderService;
import com.gestion.service.product.ProductDiscountedPriceCalculatorService;
import com.gestion.service.product.ProductFinderByIdAndCommerceIdService;
import com.gestion.service.sale.SaleProductValidatorService;
import com.gestion.service.sale.SaleStockValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SaleDetailsValidatorService {
    private final ProductFinderByIdAndCommerceIdService productFinderByIdAndCommerceIdService;
    private final SaleProductValidatorService saleProductValidatorService;
    private final SaleStockValidatorService saleStockValidatorService;
    private final ProductActiveOfferFinderService productActiveOfferFinderService;
    private final ProductDiscountedPriceCalculatorService productDiscountedPriceCalculatorService;

    public SaleDetailsValidationResult validate(Long commerceId, Map<Long, Integer> quantitiesByProductId) {
        List<SaleDetail> details = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        // única fuente de verdad del "ahora" para decidir qué ofertas están vigentes
        Instant now = Instant.now().truncatedTo(ChronoUnit.MICROS);

        for (Map.Entry<Long, Integer> entry : quantitiesByProductId.entrySet()) {
            Product product = productFinderByIdAndCommerceIdService
                    .findProductByIdAndCommerceId(entry.getKey(), commerceId);

            saleProductValidatorService.validateProductIsAvailable(product);
            saleStockValidatorService.validateStock(product, entry.getValue());

            // precio vigente: salePrice con el descuento de la oferta activa, o salePrice si no hay
            Offer activeOffer = productActiveOfferFinderService.findActiveProductOffer(product, now);
            BigDecimal unitPrice = (activeOffer != null)
                    ? productDiscountedPriceCalculatorService.calculateDiscountedPrice(product.getSalePrice(), activeOffer)
                    : product.getSalePrice();
            
            SaleDetail detail = new SaleDetail();
            detail.setProduct(product);
            detail.setPrice(unitPrice);
            detail.setQuantity(entry.getValue());

            details.add(detail);
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(entry.getValue())));
        }

        return new SaleDetailsValidationResult(details, subtotal);
    }


}
