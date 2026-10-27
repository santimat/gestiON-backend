package com.gestion.service.sale;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.request.sale.SaleDetailRequest;
import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.dto.response.sale.SaleResponse;
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
        sale.setTotal(subtotal);
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
}
