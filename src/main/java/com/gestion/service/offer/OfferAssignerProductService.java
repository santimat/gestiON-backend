package com.gestion.service.offer;

import com.gestion.dto.request.offer.OfferProductIdsRequest;
import com.gestion.enums.ErrorCode;
import com.gestion.exception.OfferHasPaymentMethodsException;
import com.gestion.model.Offer;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import com.gestion.service.product.ProductFinderByIdsAndCommerceIdService;
import com.gestion.service.product.ProductFinderByOfferIdAndCommerceIdService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class OfferAssignerProductService {

    private final ProductFinderByIdsAndCommerceIdService productFinderByIdsAndCommerceIdService;
    private final ProductFinderByOfferIdAndCommerceIdService productFinderByOfferIdAndCommerceIdService;
    private final OfferFinderByIdAndCommerceIdService offerFinderByIdAndCommerceIdService;
    private final OfferProductPriceValidatorService offerProductPriceValidatorService;
    private final JpaProductRepository productRepository;


    @Transactional
    public void assignOfferToProducts(Long offerId, OfferProductIdsRequest request, Long commerceId) {
        Offer offer = offerFinderByIdAndCommerceIdService.findOfferByIdAndCommerceId(offerId, commerceId);

        if (offer.getPaymentMethods() != null && !offer.getPaymentMethods().isEmpty()) {
            throw new OfferHasPaymentMethodsException(
                    ErrorCode.OFFER_HAS_PAYMENT_METHODS,
                    "Offers with payment methods cannot be applied to products");
        }

        List<Product> productsToUnassign = productFinderByOfferIdAndCommerceIdService
                .findProductsByOfferIdAndCommerceId(offerId, commerceId);

        List<Product> productsToAssign = productFinderByIdsAndCommerceIdService
                .findProductsByIdsAndCommerceId(request.productIds(), commerceId);

        // validar que el valor de la oferta no deje el precio del producto negativo
        offerProductPriceValidatorService.validateOfferNotGreaterThanProductPrices(offer, productsToAssign);

        for (Product product : productsToUnassign) {
            product.setOffer(null);
        }

        for (Product product : productsToAssign) {
            product.setOffer(offer);
        }

        // unica lista para guardar con .saveAll()
        List<Product> productsToSave =
                Stream.concat(productsToUnassign.stream(), productsToAssign.stream()).distinct().toList();

        productRepository.saveAll(productsToSave);
    }

}