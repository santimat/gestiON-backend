package com.gestion.service.offer;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidDateException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OfferDateValidatorService {

    public void validateOfferDates(Instant startDate, Instant endDate, Instant now) {
        if (startDate.isBefore(now)) {
            throw new InvalidDateException(ErrorCode.INVALID_DATE, "Offer start date cannot be in the past");
        }
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateException(ErrorCode.INVALID_DATE, "Offer end date cannot be before start date");
        }
    }

    public void validateOfferDatesForUpdate(Instant startDate, Instant endDate, Instant now, Instant originalStartDate) {
        // Se permite conservar la fecha de inicio original, aunque ya pertenezca al pasado.
        boolean keepsOriginalStartDate = originalStartDate != null
                && startDate.truncatedTo(java.time.temporal.ChronoUnit.MICROS)
                        .equals(originalStartDate.truncatedTo(java.time.temporal.ChronoUnit.MICROS));

        if (!keepsOriginalStartDate && startDate.isBefore(now)) {
            throw new InvalidDateException(ErrorCode.INVALID_DATE, "Offer start date cannot be moved to the past");
        }
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateException(ErrorCode.INVALID_DATE, "Offer end date cannot be before start date");
        }
    }
}
