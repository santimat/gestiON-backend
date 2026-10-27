package com.gestion.service.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.sale.SaleDetailRequest;
import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.response.sale.SaleResponse;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidDiscountException;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.*;
import com.gestion.repository.JpaSaleDetailRepository;
import com.gestion.repository.JpaSaleRepository;
import com.gestion.service.product.ProductFinderByIdAndCommerceIdService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SaleCreatorService {
    private final JpaSaleRepository jpaSaleRepository;
    private final JpaSaleDetailRepository jpaSaleDetailRepository;
    private final ProductFinderByIdAndCommerceIdService productFinderByIdAndCommerceIdService;
    private final SaleProductValidatorService saleProductValidatorService;
    private final SaleStockValidatorService saleStockValidatorService;
    private final SaleClientResolverService saleClientResolverService;
    private final SaleStatusResolverService saleStatusResolverService;
    private final EntityManager entityManager;

    @Transactional
    public SaleResponse createSale(SaleRequest request, UserPrincipal authenticatedUser) {
        if (authenticatedUser.getCommerceId() == null) {
            throw new AccessDeniedException("Authenticated user does not have a commerce associated");
        }

        // merge de items repetidos del mismo produto sumando cantidades
        Map<Long, Integer> quantitiesByProductId = new LinkedHashMap<>();
        for (SaleDetailRequest detailRequest : request.details()) {
            quantitiesByProductId.merge(detailRequest.productId(), detailRequest.quantity(), Integer::sum);
        }

        // validaciones previas: si algo falla, no se descuenta stock ni se persiste nada
        List<SaleDetail> details = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantitiesByProductId.entrySet()) {
            Product product = productFinderByIdAndCommerceIdService
                    .findProductByIdAndCommerceId(entry.getKey(), authenticatedUser.getCommerceId());

            saleProductValidatorService.validateProductIsAvailable(product);
            saleStockValidatorService.validateStock(product, entry.getValue());

            SaleDetail detail = new SaleDetail();
            detail.setProduct(product);
            detail.setPrice(product.getSalePrice());
            detail.setQuantity(entry.getValue());

            details.add(detail);
            subtotal = subtotal.add(product.getSalePrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }

        // armado de la venta
        Sale sale = SaleMapper.toEntity(request);
        sale.setUser(entityManager.getReference(com.gestion.model.User.class, authenticatedUser.getId()));
        sale.setCommerce(entityManager.getReference(Commerce.class, authenticatedUser.getCommerceId()));
        sale.setSubtotal(subtotal);

        BigDecimal totalAfterDiscount = applyDiscount(subtotal, sale);

        sale.setTotal(totalAfterDiscount);
        sale.setTotalAfterDiscount(totalAfterDiscount);
        sale.setStatus(saleStatusResolverService.resolveStatus(request));

        if (authenticatedUser.getSellPointId() != null) {
            sale.setSellPoint(entityManager.getReference(com.gestion.model.SellPoint.class,
                    authenticatedUser.getSellPointId()));
        }
        
        sale.setClient(saleClientResolverService.resolveClient(request.clientId(), authenticatedUser.getCommerceId()));

        // descuento de stock: solo se llega acá si todos los items pasaron la validación
        for (SaleDetail detail : details) {
            Product product = detail.getProduct();
            product.setCurrentStock(product.getCurrentStock() - detail.getQuantity());
        }

        Sale savedSale = jpaSaleRepository.save(sale);
        for (SaleDetail detail : details) {
            detail.setSale(savedSale);
            jpaSaleDetailRepository.save(detail);
        }
        return SaleMapper.toResponse(savedSale, details);
    }

    /**
     * Valida y aplica el descuento de la venta sobre el subtotal.
     * Reglas: el tipo y el monto van juntos (o ambos presentes, o ambos ausentes);
     * PERCENTAGE admite valores entre 1 y 100 (exclusivo 100); FIXED_AMOUNT no puede
     * superar el subtotal. Devuelve el total con el descuento ya aplicado.
     */
    private BigDecimal applyDiscount(BigDecimal subtotal, Sale sale) {
        BigDecimal discount = sale.getDiscount();

        if (sale.getDiscountType() == null) {
            if (discount != null && discount.compareTo(BigDecimal.ZERO) > 0) {
                throw new InvalidDiscountException(ErrorCode.DISCOUNT_WITHOUT_TYPE,
                        "A discount amount was provided without a discount type");
            }
            return subtotal;
        }

        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDiscountException(ErrorCode.TYPE_WITHOUT_DISCOUNT,
                    "A discount type was provided without a discount amount");
        }

        BigDecimal totalAfterDiscount;
        switch (sale.getDiscountType()) {
            case PERCENTAGE -> {
                if (discount.compareTo(BigDecimal.valueOf(100)) >= 0) {
                    throw new InvalidDiscountException(ErrorCode.INVALID_DISCOUNT,
                            "Percentage discount must be lower than 100");
                }
                BigDecimal hundred = BigDecimal.valueOf(100);
                totalAfterDiscount = subtotal.subtract(
                        subtotal.multiply(discount).divide(hundred, 2, RoundingMode.HALF_UP));
            }
            case FIXED_AMOUNT -> {
                if (discount.compareTo(subtotal) > 0) {
                    throw new InvalidDiscountException(ErrorCode.DISCOUNT_EXCEEDS_SUBTOTAL,
                            "Fixed amount discount cannot be greater than the sale subtotal");
                }
                totalAfterDiscount = subtotal.subtract(discount);
            }
            default -> throw new InvalidDiscountException(ErrorCode.INVALID_DISCOUNT,
                    "Unsupported discount type");
        }

        return totalAfterDiscount.setScale(2, RoundingMode.HALF_UP);
    }
}
