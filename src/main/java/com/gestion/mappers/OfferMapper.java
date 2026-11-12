package com.gestion.mappers;

import com.gestion.dto.request.offer.OfferRequest;
import com.gestion.dto.response.offer.OfferResponse;
import com.gestion.enums.OfferStatus;
import com.gestion.enums.OfferType;
import com.gestion.enums.PaymentMethod;
import com.gestion.model.Commerce;
import com.gestion.model.Offer;

import java.util.HashSet;
import java.util.Set;

public class OfferMapper {
    public static OfferResponse toResponse(Offer offer, OfferStatus offerStatus) {
        return new OfferResponse(
                offer.getId(),
                offer.getValue(),
                offer.getDiscountType(),
                offer.getOfferType(),
                offer.getStartDate(),
                offer.getEndDate(),
                offerStatus,
                offer.getPaymentMethods()
        );
    }

    public static Offer toEntity(OfferRequest request, Commerce commerce) {
        OfferType offerType = (request.paymentMethods() != null && !request.paymentMethods().isEmpty())
                ? OfferType.PAYMENT_METHOD
                : OfferType.PRODUCT;
        Set<PaymentMethod> paymentMethods = request.paymentMethods() == null
                ? new HashSet<>()
                : new HashSet<>(request.paymentMethods());
        return new Offer(
                null,
                commerce,
                request.value(),
                request.discountType(),
                offerType,
                request.startDate(),
                request.endDate(),
                paymentMethods
        );
    }
}