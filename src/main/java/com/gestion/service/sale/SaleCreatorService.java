package com.gestion.service.sale;

import com.gestion.dto.project.sale.SalePersistenceResult;
import com.gestion.dto.project.sale.SalePricingResult;
import com.gestion.dto.project.sale.SaleValidationResult;
import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.response.sale.SaleSummaryResponse;
import com.gestion.enums.Role;
import com.gestion.enums.SaleStatus;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Sale;
import com.gestion.service.commerce.CommerceAvailabilityValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
public class SaleCreatorService {
    private final CommerceAvailabilityValidatorService commerceAvailabilityValidator;
    private final SaleValidationService validationService;
    private final SaleEntityBuilderService entityBuilder;
    private final SaleGiftHandlerService giftHandler;
    private final SalePricingService pricingService;
    private final SaleSellPointResolverService sellPointResolver;
    private final SaleClientResolverService clientResolver;
    private final SaleStockUpdaterService stockUpdater;
    private final SalePersistenceService persistenceService;
    private final SaleTransactionService transactionService;
    private final SalePendingCalculatorService salePendingCalculatorService;

    @Transactional
    public SaleSummaryResponse createSale(SaleRequest request, Long userId, Long commerceId, Long sellPointId, Role role) {
        commerceAvailabilityValidator.checkCommerceIsAvailable(commerceId);

        // 1. Validate payment method + merge details + validate stock/prices/offers
        SaleValidationResult validation = validationService.validateAndMerge(request, commerceId);

        // 2. Build sale entity
        Sale sale = entityBuilder.build(request, userId, commerceId, validation.subtotal());

        // 3. Handle gift (sets status/total if gift)
        giftHandler.handleIfGift(request, sale, role);

        // 4. Apply pricing (discount, total, status, partial payment validation)
        SalePricingResult pricing = pricingService.applyPricing(sale, request, commerceId, validation.subtotal());
        sale.setTotal(pricing.total());
        sale.setStatus(pricing.status());

        // 5. Resolve sell point & client
        sale.setSellPoint(sellPointResolver.resolveSellPoint(sellPointId));
        sale.setClient(clientResolver.resolveClient(request.clientId(), commerceId));

        // 6. Decrement stock (skips if GIFTED)
        if (sale.getStatus() != SaleStatus.GIFTED) {
            stockUpdater.applyStockDecrement(validation.details());
        }

        // 7. Persist sale + details
        SalePersistenceResult persistence = persistenceService.persist(sale, validation.details());

        // 8. Create transaction if revenue/outflow > 0
        transactionService.createIfNeeded(request, persistence.savedSale());

        // 9. Build response
        BigDecimal totalPending = salePendingCalculatorService.calculateTotalPending(persistence.savedSale());
        return SaleMapper.toSummaryResponse(persistence.savedSale(), persistence.itemCount(), totalPending);
    }
}