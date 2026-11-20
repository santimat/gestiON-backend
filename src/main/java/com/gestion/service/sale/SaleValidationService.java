package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.service.saledetail.SaleDetailMergerService;
import com.gestion.service.saledetail.SaleDetailValidationResult;
import com.gestion.service.saledetail.SaleDetailValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class SaleValidationService {
    private final SalePaymentMethodValidatorService paymentMethodValidator;
    private final SaleDetailMergerService merger;
    private final SaleDetailValidatorService detailValidator;

    public SaleValidationResult validateAndMerge(SaleRequest request, Long commerceId) {
        paymentMethodValidator.validate(request);
        Map<Long, Integer> quantities = merger.mergeDetails(request.details());
        SaleDetailValidationResult result = detailValidator.validate(commerceId, quantities);
        return new SaleValidationResult(quantities, result.details(), result.subtotal());
    }
}