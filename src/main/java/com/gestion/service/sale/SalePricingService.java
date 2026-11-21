package com.gestion.service.sale;

import com.gestion.dto.project.sale.SalePricingResult;
import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.enums.SaleStatus;
import com.gestion.model.Sale;
import com.gestion.service.offer.OfferFinderByPaymentMethodAndCommerceIdService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SalePricingService {
    private final OfferFinderByPaymentMethodAndCommerceIdService offerFinder;
    private final SaleDiscountApplierService discountApplier;
    private final SaleStatusResolverService statusResolver;
    private final SalePartialPaymentValidatorService partialPaymentValidator;

    public SalePricingResult applyPricing(Sale sale, SaleRequest request, Long commerceId, BigDecimal subtotal) {
        if (sale.getStatus() == SaleStatus.GIFTED) {
            return new SalePricingResult(BigDecimal.ZERO, SaleStatus.GIFTED);
        }

        offerFinder.findActiveByPaymentMethodAndCommerceId(sale.getPaymentMethod(), commerceId)
                .ifPresent(offer -> {
                    sale.setPaymentMethodDiscountType(offer.getDiscountType());
                    sale.setPaymentMethodDiscount(offer.getValue());
                });

        BigDecimal total = discountApplier.applyDiscount(subtotal, sale.getPaymentMethodDiscountType(), sale.getPaymentMethodDiscount());
        SaleStatus status = statusResolver.resolveStatus(request);
        partialPaymentValidator.validate(request, total);

        return new SalePricingResult(total, status);
    }
}