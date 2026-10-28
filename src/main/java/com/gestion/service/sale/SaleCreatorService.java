package com.gestion.service.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.response.sale.SaleResponse;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final EntityManager entityManager;

    @Transactional
    public SaleResponse createSale(SaleRequest request, UserPrincipal authenticatedUser) {
        if (authenticatedUser.getCommerceId() == null) {
            throw new AccessDeniedException("Authenticated user does not have a commerce associated");
        }

        // merge de items repetidos del mismo produto sumando cantidades
        Map<Long, Integer> quantitiesByProductId = saleDetailsMergerService.mergeDetails(request.details());

        // validaciones previas: si algo falla, no se descuenta stock ni se persiste nada
        SaleDetailsValidationResult validation =
                saleDetailsValidatorService.validate(authenticatedUser.getCommerceId(), quantitiesByProductId);
        List<SaleDetail> details = validation.details();
        BigDecimal subtotal = validation.subtotal();

        // armado de la venta
        Sale sale = SaleMapper.toEntity(request);
        sale.setUser(entityManager.getReference(com.gestion.model.User.class, authenticatedUser.getId()));
        sale.setCommerce(entityManager.getReference(Commerce.class, authenticatedUser.getCommerceId()));
        sale.setSubtotal(subtotal);

        BigDecimal totalAfterDiscount = saleDiscountApplierService.applyDiscount(
                subtotal, sale.getDiscountType(), sale.getDiscount());

        sale.setTotal(totalAfterDiscount);
        sale.setTotalAfterDiscount(totalAfterDiscount);
        sale.setStatus(saleStatusResolverService.resolveStatus(request));

        sale.setSellPoint(saleSellPointResolverService.resolveSellPoint(authenticatedUser.getSellPointId()));

        sale.setClient(saleClientResolverService.resolveClient(request.clientId(), authenticatedUser.getCommerceId()));

        // descuento de stock: solo se llega acá si todos los items pasaron la validación
        saleStockUpdaterService.applyStockDecrement(details);

        Sale savedSale = jpaSaleRepository.save(sale);
        saleDetailCreatorService.createDetails(savedSale, details);
        return SaleMapper.toResponse(savedSale, details);
    }
}
