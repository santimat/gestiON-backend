package com.gestion.service.offer;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.OfferHasPaymentMethodsException;
import com.gestion.model.Offer;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import com.gestion.service.product.ProductFinderByIdsAndCommerceIdService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OfferAssignerProductService {

    private final ProductFinderByIdsAndCommerceIdService productFinderByIdsAndCommerceIdService;
    private final OfferFinderByIdAndCommerceIdService offerFinderByIdAndCommerceIdService;
    private final JpaProductRepository productRepository;

    //TODO: List<Product> findAllByOfferId(Long offerId);
    //TODO: manejar la desasignación de la oferta de los productos que ya tienen una oferta asignada
    // ejemplo:
    // 1. llega {productIds: [1, 2, 3], offerId: 1}
    // 2. luego llega {productIds: [2, 3, 4], offerId: 1} se debe desasignar la oferta 1 del producto con el id 1

    //TODO: manejar la asignación de una oferta a un producto que ya tiene otra oferta asignada, ejemplo:
    // 1. llega {productIds: [1, 2, 3], offerId: 1}
    // 2. luego llega {productIds: [2, 3, 4], offerId: 2} se debe desasignar la oferta 1 de los productos con id 2, 3
    // y asignarles la oferta 2, y asignar la oferta 2 al producto con id 4


    @Transactional
    public void assignOfferToProducts(List<Long> productIds, Long offerId, Long commerceId) {
        Offer offer = offerFinderByIdAndCommerceIdService.findOfferByIdAndCommerceId(offerId, commerceId);

        if (offer.getPaymentMethods() != null && !offer.getPaymentMethods().isEmpty()) {
            throw new OfferHasPaymentMethodsException(
                    ErrorCode.OFFER_HAS_PAYMENT_METHODS,
                    "Offers with payment methods cannot be applied to products");
        }

        List<Product> products = productFinderByIdsAndCommerceIdService
                .findProductsByIdsAndCommerceId(productIds, commerceId);

        for (Product product : products) {
            
            product.setOffer(offer);
        }

        productRepository.saveAll(products);
    }

    @Transactional
    public void assignProductsToOffer(List<Long> productIds, Long offerId, Long commerceId) {
        assignOfferToProducts(productIds, offerId, commerceId);
    }

    @Transactional
    public void assignOffer(Long offerId, List<Long> productIds, Long commerceId) {
        assignOfferToProducts(productIds, offerId, commerceId);
    }

    @Transactional
    public void assign(List<Long> productIds, Long offerId, Long commerceId) {
        assignOfferToProducts(productIds, offerId, commerceId);
    }
}