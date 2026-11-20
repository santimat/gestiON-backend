package com.gestion.service.offer;

import com.gestion.service.offer.OfferDateValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@AllArgsConstructor
public class OfferDateValidator {
    private final OfferDateValidatorService delegate;

    public void validateForCreate(Instant startDate, Instant endDate, Instant now) {
        delegate.validateOfferDates(startDate, endDate, now);
    }

    public void validateForUpdate(Instant startDate, Instant endDate, Instant now, Instant originalStartDate) {
        delegate.validateOfferDatesForUpdate(startDate, endDate, now, originalStartDate);
    }
}