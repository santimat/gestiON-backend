package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.request.transaction.TransactionRequest;
import com.gestion.dto.response.sale.SaleSummaryResponse;
import com.gestion.enums.Role;
import com.gestion.enums.SaleStatus;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleRepository;
import com.gestion.service.commerce.CommerceAvailabilityValidatorService;
import com.gestion.service.offer.OfferFinderByPaymentMethodAndCommerceIdService;
import com.gestion.service.saledetail.SaleDetailCreatorService;
import com.gestion.service.saledetail.SaleDetailMergerService;
import com.gestion.service.saledetail.SaleDetailValidationResult;
import com.gestion.service.saledetail.SaleDetailValidatorService;
import com.gestion.service.transaction.TransactionCreatorService;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SaleCreatorService {
    private final JpaSaleRepository jpaSaleRepository;
    private final SaleClientResolverService saleClientResolverService;
    private final SaleStatusResolverService saleStatusResolverService;
    private final SaleDiscountApplierService saleDiscountApplierService;
    private final SaleDetailMergerService saleDetailMergerService;
    private final SaleDetailValidatorService saleDetailValidatorService;
    private final SaleSellPointResolverService saleSellPointResolverService;
    private final SaleStockUpdaterService saleStockUpdaterService;
    private final SaleDetailCreatorService saleDetailCreatorService;
    private final OfferFinderByPaymentMethodAndCommerceIdService offerFinderByPaymentMethodAndCommerceIdService;
    private final CommerceAvailabilityValidatorService commerceAvailabilityValidatorService;
    private final SalePaymentMethodValidatorService salePaymentMethodValidatorService;
    private final SalePartialPaymentValidatorService salePartialPaymentValidatorService;
    private final TransactionCreatorService transactionCreatorService;
    private final SaleGiftHandlerService saleGiftHandler;
    private final SaleRevenueOutflowCalculatorService saleRevenueOutflowCalculator;
    private final EntityManager entityManager;

    @Transactional
    public SaleSummaryResponse createSale(SaleRequest request, Long userId, Long commerceId, Long sellPointId, Role role) {
        commerceAvailabilityValidatorService.checkCommerceIsAvailable(commerceId);

        // Validación: CREDIT_ACCOUNT requiere clientId
        salePaymentMethodValidatorService.validate(request);

        // Validación: partiallyPaidAmount no puede exceder el total (se valida después de calcular total)
        // merge de items repetidos del mismo produto sumando cantidades
        Map<Long, Integer> quantitiesByProductId = saleDetailMergerService.mergeDetails(request.details());

        // validaciones previas: si algo falla, no se descuenta stock ni se persiste nada
        SaleDetailValidationResult validation =
                saleDetailValidatorService.validate(commerceId, quantitiesByProductId);
        List<SaleDetail> details = validation.details();
        BigDecimal subtotal = validation.subtotal();

        // armado de la venta
        Sale sale = SaleMapper.toEntity(request);
        sale.setUser(entityManager.getReference(com.gestion.model.User.class, userId));
        sale.setCommerce(entityManager.getReference(Commerce.class, commerceId));
        sale.setSubtotal(subtotal);

        // Manejar gift (valida y setea status/total)
        saleGiftHandler.handleIfGift(request, sale, role);

        if (sale.getStatus() != SaleStatus.GIFTED) {
            // el descuento de la venta sale exclusivamente de la oferta vigente del metodo de pago;
            // si no hay oferta, la venta queda sin descuento
            offerFinderByPaymentMethodAndCommerceIdService
                    .findActiveByPaymentMethodAndCommerceId(sale.getPaymentMethod(), commerceId)
                    .ifPresent(offer -> {
                        sale.setPaymentMethodDiscountType(offer.getDiscountType());
                        sale.setPaymentMethodDiscount(offer.getValue());
                    });

            BigDecimal totalAfterDiscount = saleDiscountApplierService.applyDiscount(
                    subtotal, sale.getPaymentMethodDiscountType(), sale.getPaymentMethodDiscount());

            sale.setTotal(totalAfterDiscount);
            sale.setStatus(saleStatusResolverService.resolveStatus(request));

            // Validación: partiallyPaidAmount no puede exceder el total
            salePartialPaymentValidatorService.validate(request, sale.getTotal());
        }

        sale.setSellPoint(saleSellPointResolverService.resolveSellPoint(sellPointId));

        sale.setClient(saleClientResolverService.resolveClient(request.clientId(), commerceId));

        // descuento de stock: solo se llega acá si todos los items pasaron la validación
        saleStockUpdaterService.applyStockDecrement(details);

        Sale savedSale = jpaSaleRepository.save(sale);
        saleDetailCreatorService.createDetails(savedSale, details);

        // Calcular revenue/outflow y crear Transaction
        SaleRevenueOutflowCalculatorService.Result revenueOutflow = saleRevenueOutflowCalculator.calculate(request, savedSale);
        if (revenueOutflow.revenue().compareTo(BigDecimal.ZERO) > 0 || revenueOutflow.outflow().compareTo(BigDecimal.ZERO) > 0) {
            TransactionRequest txRequest =
                    new TransactionRequest(
                            revenueOutflow.revenue(), revenueOutflow.outflow(), savedSale.getId());
            transactionCreatorService.createTransaction(txRequest, commerceId);
        }

        // el summary expone unidades vendidas (SUM quantity), igual que el listado
        int itemCount = details.stream().mapToInt(SaleDetail::getQuantity).sum();
        return SaleMapper.toSummaryResponse(savedSale, itemCount);
    }
}
