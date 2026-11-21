package com.gestion.service.product;

import com.gestion.dto.project.product.ProductUpdateValidationResult;
import com.gestion.dto.request.product.ProductUpdateRequest;
import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.model.Offer;
import com.gestion.model.Product;
import com.gestion.service.category.CategoryFinderByIdAndCommerceIdService;
import com.gestion.service.commerce.CommerceFinderByIdService;
import com.gestion.service.offer.OfferProductPriceValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductUpdateValidator {
    private final ProductFinderByIdAndCommerceIdService productFinder;
    private final CategoryFinderByIdAndCommerceIdService categoryFinder;
    private final CommerceFinderByIdService commerceFinder;
    private final ProductPriceValidatorService priceValidator;
    private final OfferProductPriceValidatorService offerPriceValidator;

    public ProductUpdateValidationResult validate(Long productId, ProductUpdateRequest request, Long commerceId) {
        Product product = productFinder.findProductByIdAndCommerceId(productId, commerceId);
        Commerce commerce = commerceFinder.findCommerceById(commerceId);
        Category category = categoryFinder.findCategoryByIdAndCommerceId(request.categoryId(), commerceId);

        priceValidator.validateSalePriceNotLowerThanCostPrice(request.costPrice(), request.salePrice());

        Offer assignedOffer = product.getOffer();
        if (assignedOffer != null) {
            offerPriceValidator.validateOfferNotGreaterThanProductPrice(assignedOffer, request.salePrice());
        }

        return new ProductUpdateValidationResult(product, commerce, category);
    }
}