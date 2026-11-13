package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.response.sale.SaleSummaryResponse;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleRepository;
import com.gestion.service.commerce.CommerceAvailabilityValidatorService;
import com.gestion.service.offer.OfferFinderByPaymentMethodAndCommerceIdService;
import com.gestion.service.saleDetails.SaleDetailCreatorService;
import com.gestion.service.saleDetails.SaleDetailsMergerService;
import com.gestion.service.saleDetails.SaleDetailsValidationResult;
import com.gestion.service.saleDetails.SaleDetailsValidatorService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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
    private final SaleDetailsMergerService saleDetailsMergerService;
    private final SaleDetailsValidatorService saleDetailsValidatorService;
    private final SaleSellPointResolverService saleSellPointResolverService;
    private final SaleStockUpdaterService saleStockUpdaterService;
    private final SaleDetailCreatorService saleDetailCreatorService;
    private final OfferFinderByPaymentMethodAndCommerceIdService offerFinderByPaymentMethodAndCommerceIdService;
    private final CommerceAvailabilityValidatorService commerceAvailabilityValidatorService;
    private final EntityManager entityManager;

    @Transactional
    public SaleSummaryResponse createSale(SaleRequest request, Long userId, Long commerceId, Long sellPointId) {
        commerceAvailabilityValidatorService.checkCommerceIsAvailable(commerceId);

        // merge de items repetidos del mismo produto sumando cantidades
        Map<Long, Integer> quantitiesByProductId = saleDetailsMergerService.mergeDetails(request.details());

        // validaciones previas: si algo falla, no se descuenta stock ni se persiste nada
        SaleDetailsValidationResult validation =
                saleDetailsValidatorService.validate(commerceId, quantitiesByProductId);
        List<SaleDetail> details = validation.details();
        BigDecimal subtotal = validation.subtotal();

        // armado de la venta
        Sale sale = SaleMapper.toEntity(request);
        sale.setUser(entityManager.getReference(com.gestion.model.User.class, userId));
        sale.setCommerce(entityManager.getReference(Commerce.class, commerceId));
        sale.setSubtotal(subtotal);

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

        sale.setSellPoint(saleSellPointResolverService.resolveSellPoint(sellPointId));

        sale.setClient(saleClientResolverService.resolveClient(request.clientId(), commerceId));

        // descuento de stock: solo se llega acá si todos los items pasaron la validación
        saleStockUpdaterService.applyStockDecrement(details);

        Sale savedSale = jpaSaleRepository.save(sale);
        saleDetailCreatorService.createDetails(savedSale, details);

        // el summary expone unidades vendidas (SUM quantity), igual que el listado
        int itemCount = details.stream().mapToInt(SaleDetail::getQuantity).sum();
        return SaleMapper.toSummaryResponse(savedSale, itemCount);
    }
}
